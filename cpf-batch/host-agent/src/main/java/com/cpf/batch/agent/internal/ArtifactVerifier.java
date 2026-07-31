package com.cpf.batch.agent.internal;

import com.cpf.batch.agent.AgentProperties;
import com.cpf.batch.api.AgentArtifactRequest;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

/** keyId 기반 Trust Store와 유효기간·폐기 정책으로 Artifact manifest 서명을 검증합니다. */
public final class ArtifactVerifier {
    private final AgentProperties properties;

    public ArtifactVerifier(AgentProperties properties) { this.properties = properties; }

    public Verified verify(Path file, AgentArtifactRequest request, long size) throws Exception {
        String actual = digest(file);
        if (!actual.equalsIgnoreCase(request.sha256())) throw new SecurityException("ARTIFACT_CHECKSUM_MISMATCH");
        if (!properties.isRequireSignature()) throw new SecurityException("ARTIFACT_UNSIGNED_MODE_PROHIBITED");
        if (request.signatureBase64() == null || request.signatureBase64().isBlank()
                || request.keyId() == null || request.keyId().isBlank()) {
            throw new SecurityException("ARTIFACT_SIGNATURE_KEY_REQUIRED");
        }
        String canonical = canonical(request, size, actual);
        verifySignature(request.keyId(), request.signatureBase64(), canonical);
        return new Verified(actual, size, canonical);
    }

    public Verified verifyStored(Path file, Properties state, AgentProperties.ServiceDefinition service) throws Exception {
        long size = Files.size(file);
        String actual = digest(file);
        String expected = required(state, "sha256");
        if (!actual.equalsIgnoreCase(expected)) throw new SecurityException("ARTIFACT_ROLLBACK_DIGEST_MISMATCH");
        long expectedSize = Long.parseLong(required(state, "size"));
        if (size != expectedSize) throw new SecurityException("ARTIFACT_ROLLBACK_SIZE_MISMATCH");
        if (!required(state, "serviceId").equals(service.getServiceId())
                || !required(state, "environment").equals(service.getEnvironmentCode())
                || !required(state, "channel").equals(service.getReleaseChannel())) {
            throw new SecurityException("ARTIFACT_ROLLBACK_ENVIRONMENT_BINDING_MISMATCH");
        }
        AgentArtifactRequest request = new AgentArtifactRequest(
                required(state, "serviceId"), required(state, "coordinate"), required(state, "version"), actual,
                required(state, "signatureBase64"), required(state, "runtimeMode"), state.getProperty("configRef", ""),
                "cpf-batch-host-agent", "verified rollback", Long.parseLong(required(state, "releaseSequence")),
                required(state, "environment"), required(state, "channel"), required(state, "keyId"));
        String canonical = canonical(request, size, actual);
        verifySignature(request.keyId(), request.signatureBase64(), canonical);
        return new Verified(actual, size, canonical);
    }

    private void verifySignature(String keyId, String signatureBase64, String canonical) throws Exception {
        AgentProperties.TrustedKey trusted = properties.getArtifactTrustStore().get(keyId);
        if (trusted == null) throw new SecurityException("ARTIFACT_KEY_ID_NOT_TRUSTED");
        Instant now = Instant.now();
        if (trusted.isRevoked()) throw new SecurityException("ARTIFACT_SIGNING_KEY_REVOKED");
        if (trusted.getNotBefore() != null && now.isBefore(trusted.getNotBefore())) throw new SecurityException("ARTIFACT_SIGNING_KEY_NOT_YET_VALID");
        if (trusted.getNotAfter() != null && !now.isBefore(trusted.getNotAfter())) throw new SecurityException("ARTIFACT_SIGNING_KEY_EXPIRED");
        Path keyPath = Path.of(Objects.requireNonNull(trusted.getPublicKeyPath(), "trusted public key path"))
                .toAbsolutePath().normalize();
        if (Files.isSymbolicLink(keyPath) || !Files.isRegularFile(keyPath, LinkOption.NOFOLLOW_LINKS)) {
            throw new SecurityException("ARTIFACT_TRUST_KEY_PATH_UNSAFE");
        }
        String pem = Files.readString(keyPath, StandardCharsets.US_ASCII)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s", "");
        PublicKey key = KeyFactory.getInstance("Ed25519")
                .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pem)));
        Signature signature = Signature.getInstance("Ed25519");
        signature.initVerify(key);
        signature.update(canonical.getBytes(StandardCharsets.UTF_8));
        byte[] signed;
        try { signed = Base64.getDecoder().decode(signatureBase64); }
        catch (IllegalArgumentException failure) { throw new SecurityException("ARTIFACT_SIGNATURE_ENCODING_INVALID", failure); }
        if (!signature.verify(signed)) throw new SecurityException("ARTIFACT_SIGNATURE_INVALID");
    }

    static String canonical(AgentArtifactRequest request, long size, String digest) {
        return String.join("\n", "cpf-artifact-manifest-v1",
                "serviceId=" + clean(request.serviceId()),
                "coordinate=" + clean(request.coordinate()),
                "version=" + clean(request.version()),
                "sha256=" + digest.toLowerCase(Locale.ROOT),
                "size=" + size,
                "runtimeMode=" + clean(request.runtimeMode()),
                "environment=" + clean(request.environmentCode()),
                "channel=" + clean(request.channel()),
                "releaseSequence=" + request.releaseSequence(),
                "keyId=" + clean(request.keyId()));
    }

    private static String required(Properties state, String name) {
        String value = state.getProperty(name, "").trim();
        if (value.isEmpty()) throw new SecurityException("ARTIFACT_STATE_FIELD_MISSING:" + name);
        return value;
    }
    private static String clean(String value) {
        if (value == null || value.isBlank() || value.indexOf('\n') >= 0 || value.indexOf('\r') >= 0) {
            throw new SecurityException("ARTIFACT_MANIFEST_FIELD_INVALID");
        }
        return value.trim();
    }
    private static String digest(Path file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(file, LinkOption.NOFOLLOW_LINKS)) {
            byte[] buffer = new byte[8192];
            for (int read; (read = input.read(buffer)) >= 0;) if (read > 0) digest.update(buffer, 0, read);
        }
        return HexFormat.of().formatHex(digest.digest());
    }
    public record Verified(String sha256, long size, String canonicalManifest) { }
}
