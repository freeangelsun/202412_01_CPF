package com.cpf.batch.worker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.Signature;
import java.util.Base64;
import java.util.HexFormat;
import org.junit.jupiter.api.Test;

class JcaScriptArtifactVerifierTest {
    @Test
    void verifiesDetachedSignatureWithTrustedCatalogKey() throws Exception {
        var keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
        Path artifact = Files.createTempFile("cpf-signed-script-", ".bin");
        byte[] content = "echo cpf".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        Files.write(artifact, content);
        try {
            Signature signer = Signature.getInstance("SHA256withRSA");
            signer.initSign(keyPair.getPrivate());
            signer.update(content);
            WorkerOperationalProperties properties = new WorkerOperationalProperties();
            properties.setTrustedSigningKeys(java.util.Map.of("release-2026", Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded())));
            WorkerOperationalProperties.ShellDefinition definition = new WorkerOperationalProperties.ShellDefinition();
            definition.setSignatureKeyId("release-2026");
            definition.setSignatureAlgorithm("SHA256withRSA");
            definition.setSignature(Base64.getEncoder().encodeToString(signer.sign()));
            definition.setSha256(HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(content)));
            var result = new JcaScriptArtifactVerifier(properties).verify(artifact, definition);
            assertTrue(result.valid());
            assertEquals("OK", result.code());
        } finally {
            Files.deleteIfExists(artifact);
        }
    }
}
