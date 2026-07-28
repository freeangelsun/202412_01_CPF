package com.cpf.gateway.runtime;

import com.cpf.core.api.gateway.CpfGatewayPrincipal;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfApiClientSecurityPolicyTest {
    @Test
    void authenticatedApiClientBecomesTrustedPrincipal() throws Exception {
        CpfApiClientSecurityPolicy policy = new CpfApiClientSecurityPolicy(
                Clock.fixed(Instant.parse("2026-07-28T00:00:00Z"), ZoneOffset.UTC));
        policy.replace(1L, Map.of("PARTNER-A", new CpfApiClientSecurityPolicy.Client(
                "PARTNER-A",
                sha256("secret-key"),
                true,
                Set.of("10.0.0.0/8"),
                Set.of("A1B2"),
                Instant.parse("2026-07-29T00:00:00Z"),
                10,
                60_000,
                Set.of("MBR_READ"))));

        CpfGatewayPrincipal principal = policy.authenticate("secret-key", "10.10.1.2", "A1:B2");

        assertTrue(principal.authenticated());
        assertEquals("PARTNER-A", principal.principalId());
        assertEquals(Set.of("MBR_READ"), principal.authorities());
        assertEquals("API_KEY", principal.attributes().get("authType"));
    }

    @Test
    void invalidCredentialNeverBecomesPrincipal() throws Exception {
        CpfApiClientSecurityPolicy policy = new CpfApiClientSecurityPolicy();
        policy.replace(1L, Map.of("PARTNER-A", new CpfApiClientSecurityPolicy.Client(
                "PARTNER-A", sha256("secret-key"), true, Set.of(), Set.of(), null,
                0, 60_000, Set.of("MBR_READ"))));

        CpfGatewayPrincipal principal = policy.authenticate("wrong-key", "127.0.0.1", "");

        assertFalse(principal.authenticated());
        assertTrue(principal.authorities().isEmpty());
    }

    private static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
