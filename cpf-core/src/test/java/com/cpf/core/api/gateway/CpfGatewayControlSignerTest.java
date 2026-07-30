package com.cpf.core.api.gateway;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

class CpfGatewayControlSignerTest {
    @Test
    void bodyAudienceAndKeyIdAreProtectedBySignature() {
        byte[] body = "{\"bindingId\":\"B1\"}".getBytes(StandardCharsets.UTF_8);
        String bodyHash = CpfGatewayControlSigner.sha256(body);
        String signature = CpfGatewayControlSigner.sign(
                "secret", "POST", "/internal/v1/gateway/registry/bindings",
                "application/json", bodyHash, "ADM", "operator", 1234L,
                "nonce", "gateway-1", "current");

        assertTrue(CpfGatewayControlSigner.verify(
                "secret", "POST", "/internal/v1/gateway/registry/bindings",
                "application/json", bodyHash, "ADM", "operator", 1234L,
                "nonce", "gateway-1", "current", signature));
        assertFalse(CpfGatewayControlSigner.verify(
                "secret", "POST", "/internal/v1/gateway/registry/bindings",
                "application/json", CpfGatewayControlSigner.sha256("{}".getBytes(StandardCharsets.UTF_8)),
                "ADM", "operator", 1234L, "nonce", "gateway-1", "current", signature));
        assertFalse(CpfGatewayControlSigner.verify(
                "secret", "POST", "/internal/v1/gateway/registry/bindings",
                "application/json", bodyHash, "ADM", "operator", 1234L,
                "nonce", "gateway-2", "current", signature));
        assertNotEquals(signature, CpfGatewayControlSigner.sign(
                "secret", "POST", "/internal/v1/gateway/registry/bindings",
                "application/json", bodyHash, "ADM", "operator", 1234L,
                "nonce", "gateway-1", "previous"));
    }
}
