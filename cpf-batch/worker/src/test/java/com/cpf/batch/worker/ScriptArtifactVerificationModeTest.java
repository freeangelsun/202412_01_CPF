package com.cpf.batch.worker;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ScriptArtifactVerificationModeTest {
    @Test
    void signatureIsTheDefaultAndHashOnlyRequiresExplicitOptIn() {
        WorkerOperationalProperties.ShellDefinition definition = new WorkerOperationalProperties.ShellDefinition();
        Sha256ScriptArtifactVerifier hashOnly = new Sha256ScriptArtifactVerifier();
        assertFalse(hashOnly.supports(definition));
        definition.setVerificationMode("HASH_ONLY");
        assertTrue(hashOnly.supports(definition));
    }
}
