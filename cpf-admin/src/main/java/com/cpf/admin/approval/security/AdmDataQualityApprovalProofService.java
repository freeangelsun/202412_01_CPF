package com.cpf.admin.approval.security;

import com.cpf.core.spi.data.quality.CpfDataQualityCorrectionPort;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/** Server-only signer/verifier for data-quality correction execution capabilities. */
public final class AdmDataQualityApprovalProofService {
    private final byte[] key;
    public AdmDataQualityApprovalProofService(String base64Key) {
        try { this.key = Base64.getDecoder().decode(base64Key); }
        catch (RuntimeException invalid) { throw new IllegalStateException("approval-proof-key-base64 must be valid Base64", invalid); }
        if (key.length != 32) throw new IllegalStateException("approval-proof-key-base64 must decode to 32 bytes");
    }
    public String sign(String quarantineId,long expectedVersion,String approvalRef,String payloadHash,String nonce,Instant approvedAt) {
        return hmac(message(quarantineId, expectedVersion, approvalRef, payloadHash, nonce, approvedAt));
    }
    public boolean verify(CpfDataQualityCorrectionPort.ApprovedCorrection command) {
        String expected = sign(command.quarantineId(), command.expectedVersion(), command.approvalExecutionReference(),
                command.payloadHash(), command.nonce(), command.approvedAt());
        return MessageDigest.isEqual(expected.getBytes(StandardCharsets.US_ASCII),
                command.proof().toLowerCase().getBytes(StandardCharsets.US_ASCII));
    }
    private String hmac(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception failure) { throw new IllegalStateException("HmacSHA256 unavailable", failure); }
    }
    private static String message(String id,long version,String ref,String hash,String nonce,Instant at) {
        return id + "\n" + version + "\n" + ref + "\n" + hash.toLowerCase() + "\n" + nonce + "\n" + at.toString();
    }
}
