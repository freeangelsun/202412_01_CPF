package com.cpf.batch.worker;

import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;

/** 명시적으로 HASH_ONLY가 승인된 비운영 Catalog Entry만 SHA-256으로 검증합니다. */
@Component
public class Sha256ScriptArtifactVerifier implements ScriptArtifactVerifier {
    @Override
    public boolean supports(WorkerOperationalProperties.ShellDefinition definition) {
        return "HASH_ONLY".equalsIgnoreCase(definition.getVerificationMode())
                && (definition.getSignature() == null || definition.getSignature().isBlank());
    }

    @Override
    public VerificationResult verify(Path artifact, WorkerOperationalProperties.ShellDefinition definition) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(artifact)) {
            byte[] buffer = new byte[8192];
            for (int read; (read = input.read(buffer)) >= 0;) {
                if (read > 0) {
                    digest.update(buffer, 0, read);
                }
            }
        }
        String actual = HexFormat.of().formatHex(digest.digest());
        String expected = definition.getSha256();
        if (expected == null || expected.isBlank()) {
            return VerificationResult.invalid("SHA256_REQUIRED", actual);
        }
        return actual.equalsIgnoreCase(expected.trim())
                ? VerificationResult.valid(actual)
                : VerificationResult.invalid("SHA256_MISMATCH", actual);
    }
}
