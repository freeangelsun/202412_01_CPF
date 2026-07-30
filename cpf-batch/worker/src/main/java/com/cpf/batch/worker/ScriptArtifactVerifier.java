package com.cpf.batch.worker;

import java.nio.file.Path;

/** Approved Shell Artifact의 Hash/Signature 검증 확장 SPI입니다. */
public interface ScriptArtifactVerifier {
    boolean supports(WorkerOperationalProperties.ShellDefinition definition);

    VerificationResult verify(Path artifact, WorkerOperationalProperties.ShellDefinition definition) throws Exception;

    record VerificationResult(boolean valid, String code, String artifactHash) {
        public static VerificationResult valid(String artifactHash) {
            return new VerificationResult(true, "OK", artifactHash);
        }

        public static VerificationResult invalid(String code, String artifactHash) {
            return new VerificationResult(false, code, artifactHash);
        }
    }
}
