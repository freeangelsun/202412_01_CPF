package com.cpf.admin.approval.security;

import com.cpf.data.spi.quality.CpfDataQualityCorrectionPort;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Server-only signer/verifier for short-lived, single-use data-quality correction capabilities. */
public final class AdmDataQualityApprovalProofService {
    public record IssuedCapability(String nonce,String proof,Instant approvedAt,Instant expiresAt) {}

    private final byte[] key;
    private final AdmApprovalCapabilityNonceRepository nonceRepository;
    private final Duration ttl;
    private final Clock clock;

    /** Compatibility constructor used only by isolated unit/reference tests; single-use issuance is unavailable. */
    public AdmDataQualityApprovalProofService(String base64Key) {
        this(base64Key,null,Duration.ofMinutes(15),Clock.systemUTC());
    }

    public AdmDataQualityApprovalProofService(
            String base64Key,
            AdmApprovalCapabilityNonceRepository nonceRepository,
            Duration ttl,
            Clock clock) {
        try { this.key = Base64.getDecoder().decode(base64Key); }
        catch (RuntimeException invalid) { throw new IllegalStateException("approval proof key must be valid Base64", invalid); }
        if (key.length != 32) throw new IllegalStateException("approval proof key must decode to 32 bytes");
        this.nonceRepository=nonceRepository;
        this.ttl=Objects.requireNonNull(ttl,"ttl");
        if(ttl.isZero()||ttl.isNegative()) throw new IllegalArgumentException("capability ttl must be positive");
        this.clock=Objects.requireNonNull(clock,"clock");
    }

    /** Issues and durably registers a nonce before the provider mutation can be attempted. */
    public IssuedCapability issue(
            String quarantineId,long expectedVersion,String approvalRef,String payloadHash) {
        if(nonceRepository==null) throw new SecurityException("durable approval capability nonce repository is required");
        Instant approvedAt=clock.instant();
        Instant expiresAt=approvedAt.plus(ttl);
        String nonce=UUID.randomUUID().toString();
        nonceRepository.issue(nonce,approvalRef,expiresAt);
        return new IssuedCapability(nonce,
                sign(quarantineId,expectedVersion,approvalRef,payloadHash,nonce,approvedAt),
                approvedAt,expiresAt);
    }

    public String sign(String quarantineId,long expectedVersion,String approvalRef,String payloadHash,String nonce,Instant approvedAt) {
        return hmac(message(quarantineId, expectedVersion, approvalRef, payloadHash, nonce, approvedAt));
    }

    /** Cryptographic/TTL verification only; providers may use this as defense-in-depth without consuming twice. */
    public boolean verify(CpfDataQualityCorrectionPort.ApprovedCorrection command) {
        if(command==null||command.approvedAt()==null) return false;
        Instant now=clock.instant();
        if(command.approvedAt().isAfter(now.plusSeconds(30))) return false;
        if(!now.isBefore(command.approvedAt().plus(ttl))) return false;
        String expected = sign(command.quarantineId(), command.expectedVersion(), command.approvalExecutionReference(),
                command.payloadHash(), command.nonce(), command.approvedAt());
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                command.proof().toLowerCase(java.util.Locale.ROOT).getBytes(StandardCharsets.US_ASCII));
    }

    /** Framework gateway verification: valid proof + TTL + cluster-safe atomic single-use nonce consumption. */
    public void verifyAndConsume(CpfDataQualityCorrectionPort.ApprovedCorrection command) {
        if(!verify(command)) throw new SecurityException("approval execution proof is invalid or expired");
        if(nonceRepository==null) throw new SecurityException("durable nonce repository is required");
        if(!nonceRepository.consume(command.nonce(),command.approvalExecutionReference(),clock.instant(),"DQ_CORRECTION_GATEWAY")) {
            throw new SecurityException("approval execution capability was already consumed or expired");
        }
    }

    private String hmac(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception failure) { throw new IllegalStateException("HmacSHA256 unavailable", failure); }
    }

    private static String message(String id,long version,String ref,String hash,String nonce,Instant at) {
        return id + "\n" + version + "\n" + ref + "\n" + hash.toLowerCase(java.util.Locale.ROOT) + "\n" + nonce + "\n" + at.toString();
    }
}
