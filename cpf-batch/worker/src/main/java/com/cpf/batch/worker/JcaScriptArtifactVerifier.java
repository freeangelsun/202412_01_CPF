package com.cpf.batch.worker;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.cert.CertPathValidator;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.cert.PKIXParameters;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Locale;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Catalog의 Detached Signature를 제품 Trust Store 공개키로 검증합니다. */
@Component
@Order(0)
public final class JcaScriptArtifactVerifier implements ScriptArtifactVerifier {
    private final WorkerOperationalProperties properties;

    public JcaScriptArtifactVerifier(WorkerOperationalProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean supports(WorkerOperationalProperties.ShellDefinition definition) {
        return "SIGNATURE".equalsIgnoreCase(definition.getVerificationMode())
                && definition.getSignature() != null && !definition.getSignature().isBlank();
    }

    @Override
    public VerificationResult verify(Path artifact, WorkerOperationalProperties.ShellDefinition definition) throws Exception {
        String keyId = required(definition.getSignatureKeyId(), "signatureKeyId");
        String encodedKey = properties.getTrustedSigningKeys().get(keyId);
        if (encodedKey == null || encodedKey.isBlank()) {
            return VerificationResult.invalid("SIGNING_KEY_NOT_TRUSTED", sha256(artifact));
        }
        String algorithm = required(definition.getSignatureAlgorithm(), "signatureAlgorithm");
        if (!algorithm.matches("SHA(256|384|512)with(RSA|ECDSA)")) {
            return VerificationResult.invalid("SIGNATURE_ALGORITHM_NOT_ALLOWED", sha256(artifact));
        }
        PublicKey publicKey = parseTrustedKey(encodedKey, algorithm);
        Signature verifier = Signature.getInstance(algorithm);
        verifier.initVerify(publicKey);
        try (InputStream input = Files.newInputStream(artifact)) {
            byte[] buffer = new byte[8192];
            for (int read; (read = input.read(buffer)) >= 0;) if (read > 0) verifier.update(buffer, 0, read);
        }
        String actualHash = sha256(artifact);
        String expectedHash = required(definition.getSha256(), "sha256");
        if (!MessageDigest.isEqual(actualHash.getBytes(java.nio.charset.StandardCharsets.US_ASCII),
                expectedHash.trim().toLowerCase(Locale.ROOT).getBytes(java.nio.charset.StandardCharsets.US_ASCII))) {
            return VerificationResult.invalid("SHA256_MISMATCH", actualHash);
        }
        byte[] detached;
        try { detached = Base64.getDecoder().decode(definition.getSignature().replaceAll("\\s+", "")); }
        catch (IllegalArgumentException ex) { return VerificationResult.invalid("SIGNATURE_ENCODING_INVALID", actualHash); }
        return verifier.verify(detached)
                ? VerificationResult.valid(actualHash)
                : VerificationResult.invalid("SIGNATURE_INVALID", actualHash);
    }

    private static PublicKey parseTrustedKey(String pem, String algorithm) throws Exception {
        if (pem.contains("BEGIN CERTIFICATE")) {
            return parsePkixCertificateChain(pem);
        }
        String normalized = pem.replaceAll("-----BEGIN [^-]+-----", "")
                .replaceAll("-----END [^-]+-----", "").replaceAll("\\s+", "");
        byte[] encoded = Base64.getDecoder().decode(normalized);
        String keyType = algorithm.endsWith("ECDSA") ? "EC" : "RSA";
        return KeyFactory.getInstance(keyType).generatePublic(new X509EncodedKeySpec(encoded));
    }

    /** PEM 순서 leaf→intermediate→root를 PKIX Trust Anchor 기준으로 검증합니다. */
    private static PublicKey parsePkixCertificateChain(String pem) throws Exception {
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile(
                "-----BEGIN CERTIFICATE-----(.*?)-----END CERTIFICATE-----",
                java.util.regex.Pattern.DOTALL).matcher(pem);
        java.util.List<X509Certificate> certificates = new java.util.ArrayList<>();
        CertificateFactory factory = CertificateFactory.getInstance("X.509");
        while (matcher.find()) {
            byte[] der = Base64.getMimeDecoder().decode(matcher.group(1));
            X509Certificate certificate = (X509Certificate) factory.generateCertificate(new ByteArrayInputStream(der));
            certificate.checkValidity();
            String signatureAlgorithm = certificate.getSigAlgName().toUpperCase(Locale.ROOT);
            if (signatureAlgorithm.contains("MD5") || signatureAlgorithm.contains("SHA1")) {
                throw new SecurityException("Weak certificate signature algorithm is not allowed: " + signatureAlgorithm);
            }
            certificates.add(certificate);
        }
        if (certificates.isEmpty()) throw new IllegalArgumentException("Trusted certificate is empty");
        X509Certificate trustRoot = certificates.getLast();
        if (certificates.size() > 1) {
            var path = factory.generateCertPath(certificates.subList(0, certificates.size() - 1));
            PKIXParameters parameters = new PKIXParameters(java.util.Set.of(new TrustAnchor(trustRoot, null)));
            parameters.setRevocationEnabled(false);
            CertPathValidator.getInstance("PKIX").validate(path, parameters);
        }
        return certificates.getFirst().getPublicKey();
    }

    private static String sha256(Path artifact) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(artifact)) {
            byte[] buffer = new byte[8192];
            for (int read; (read = input.read(buffer)) >= 0;) if (read > 0) digest.update(buffer, 0, read);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
}
