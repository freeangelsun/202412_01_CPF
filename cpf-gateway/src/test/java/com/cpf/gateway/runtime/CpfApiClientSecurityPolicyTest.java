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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfApiClientSecurityPolicyTest {
    @Test
    void authenticatedApiClientUsesSingleExternalRateLimitOwner() throws Exception {
        CpfApiClientSecurityPolicy policy = new CpfApiClientSecurityPolicy(
                Clock.fixed(Instant.parse("2026-07-28T00:00:00Z"), ZoneOffset.UTC));
        policy.replace(1L, Map.of("PARTNER-A", client("secret-key", 0)));

        CpfGatewayPrincipal first = policy.authenticate("secret-key", "10.10.1.2", "A1:B2");
        CpfGatewayPrincipal second = policy.authenticate("secret-key", "10.10.1.2", "A1:B2");

        assertTrue(first.authenticated());
        assertTrue(second.authenticated(), "인증 정책은 별도 local quota를 소비하면 안 된다");
        assertEquals("PARTNER-A", first.principalId());
        assertEquals("PARTNER-A", first.attributes().get("clientId"));
        assertEquals("CPF_GATEWAY_RATE_LIMIT", first.attributes().get("quotaScope"));
        assertEquals(Set.of("MBR_READ"), first.authorities());
    }

    @Test
    void invalidCredentialNeverBecomesPrincipal() throws Exception {
        CpfApiClientSecurityPolicy policy = new CpfApiClientSecurityPolicy();
        policy.replace(1L, Map.of("PARTNER-A", client("secret-key", 0)));

        CpfGatewayPrincipal principal = policy.authenticate("wrong-key", "127.0.0.1", "");

        assertFalse(principal.authenticated());
        assertTrue(principal.authorities().isEmpty());
    }

    @Test
    void staleAndSameVersionConflictsAreRejectedButIdenticalReplayIsIdempotent() throws Exception {
        CpfApiClientSecurityPolicy policy = new CpfApiClientSecurityPolicy();
        Map<String, CpfApiClientSecurityPolicy.Client> initial = Map.of(
                "PARTNER-A", client("secret-key", 0));
        policy.replace(2L, initial);

        assertEquals(2L, policy.replace(2L, initial).version());
        assertThrows(IllegalStateException.class, () -> policy.replace(1L, initial));
        assertThrows(IllegalStateException.class, () -> policy.replace(2L, Map.of(
                "PARTNER-A", client("other-key", 0))));
        assertEquals(2L, policy.snapshot().version());
    }


    @Test
    void duplicateKeyHashesAreRejectedBecauseIdentityWouldBeAmbiguous() throws Exception {
        CpfApiClientSecurityPolicy policy = new CpfApiClientSecurityPolicy();
        CpfApiClientSecurityPolicy.Client first = client("shared-key", 0);
        CpfApiClientSecurityPolicy.Client second = new CpfApiClientSecurityPolicy.Client(
                "PARTNER-B", first.keyHash(), true, Set.of(), Set.of(), null,
                0, 60_000L, Set.of("OTHER"));

        assertThrows(IllegalArgumentException.class, () -> policy.replace(
                1L, Map.of("PARTNER-A", first, "PARTNER-B", second)));
    }

    @Test
    void legacyLocalQuotaIsRejectedToPreventDualPrimary() throws Exception {
        CpfApiClientSecurityPolicy policy = new CpfApiClientSecurityPolicy();

        assertThrows(IllegalArgumentException.class, () -> policy.replace(
                1L, Map.of("PARTNER-A", client("secret-key", 1))));
    }

    private static CpfApiClientSecurityPolicy.Client client(String secret, int legacyQuota) throws Exception {
        return new CpfApiClientSecurityPolicy.Client(
                "PARTNER-A",
                sha256(secret),
                true,
                Set.of("10.0.0.0/8"),
                Set.of("A1B2"),
                Instant.parse("2099-07-29T00:00:00Z"),
                legacyQuota,
                60_000L,
                Set.of("MBR_READ"));
    }

    private static String sha256(String value) throws Exception {
        return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                .digest(value.getBytes(StandardCharsets.UTF_8)));
    }
}
