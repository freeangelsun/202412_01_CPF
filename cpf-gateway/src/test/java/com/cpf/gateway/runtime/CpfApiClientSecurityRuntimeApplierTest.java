package com.cpf.gateway.runtime;

import com.cpf.platform.operations.runtimecontrol.CpfRuntimeApplyResult;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeDelivery;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimePayload;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfApiClientSecurityRuntimeApplierTest {
    @Test
    void appliesIdempotentlyAndRejectsDualPrimaryQuotaVersionDriftAndOverflow() {
        CpfApiClientSecurityPolicy policy = new CpfApiClientSecurityPolicy();
        CpfApiClientSecurityRuntimeApplier applier = new CpfApiClientSecurityRuntimeApplier(policy);
        String validJson = payload("PARTNER-A", 0);

        CpfRuntimeApplyResult first = applier.apply(delivery(2L, validJson, "hash-1"));
        CpfRuntimeApplyResult duplicate = applier.apply(delivery(2L, validJson, "hash-1"));
        CpfRuntimeApplyResult stale = applier.apply(delivery(1L, validJson, "hash-old"));
        CpfRuntimeApplyResult drift = applier.apply(delivery(2L, payload("PARTNER-B", 0), "hash-drift"));
        CpfRuntimeApplyResult legacyQuota = applier.apply(delivery(3L, payload("PARTNER-A", 1), "hash-q"));
        CpfRuntimeApplyResult overflow = new CpfApiClientSecurityRuntimeApplier(
                new CpfApiClientSecurityPolicy()).apply(
                delivery(1L, payload("PARTNER-A", 4_294_967_297L), "hash-overflow"));

        assertTrue(first.applied());
        assertTrue(duplicate.applied());
        assertFalse(stale.applied());
        assertFalse(drift.applied());
        assertFalse(legacyQuota.applied());
        assertFalse(overflow.applied());
        assertEquals("API_CLIENT_INVALID", overflow.errorCode());
    }

    @Test
    void duplicateClientIdsFailClosedInsteadOfLastWriteWins() {
        String json = """
                {"clients":[
                  {"clientId":"PARTNER-A","keyHash":"%s","quotaPermits":0,"quotaWindowMillis":60000},
                  {"clientId":"PARTNER-A","keyHash":"%s","quotaPermits":0,"quotaWindowMillis":60000}
                ]}
                """.formatted(hash('a'), hash('b'));
        CpfRuntimeApplyResult result = new CpfApiClientSecurityRuntimeApplier(
                new CpfApiClientSecurityPolicy()).apply(delivery(1L, json, "dup"));

        assertFalse(result.applied());
        assertEquals("API_CLIENT_INVALID", result.errorCode());
    }

    private static CpfRuntimeDelivery delivery(long version, String json, String hash) {
        return new CpfRuntimeDelivery(
                "delivery-" + version,
                "change-" + version,
                "API_CLIENT",
                "gateway-1",
                version,
                version,
                hash,
                hash,
                CpfRuntimePayload.parse(json),
                1,
                Instant.parse("2099-01-01T00:00:00Z"));
    }

    private static String payload(String clientId, long quota) {
        return """
                {"clients":[{
                  "clientId":"%s",
                  "keyHash":"%s",
                  "active":true,
                  "quotaPermits":%d,
                  "quotaWindowMillis":60000,
                  "authorities":["READ"]
                }]}
                """.formatted(clientId, hash(clientId), quota);
    }

    private static String hash(char value) {
        return hash(Character.toString(value));
    }

    private static String hash(String value) {
        try {
            byte[] digest = java.security.MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException impossible) {
            throw new IllegalStateException(impossible);
        }
    }
}
