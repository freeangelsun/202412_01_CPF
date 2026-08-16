package com.cpf.admin.approval.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AdmApprovalSnapshotIntegrityTest {
    private final AdmApprovalSnapshotIntegrity integrity = new AdmApprovalSnapshotIntegrity(new ObjectMapper());

    @Test
    void equivalentFieldOrderUnicodeAndNumericRepresentationProduceSameHash() {
        Map<String, Object> left = envelope("{\"corrected\":{\"name\":\"é\",\"amount\":1.0},\"expectedVersion\":2,\"quarantineId\":\"DQ-1\"}");
        String decomposed = Normalizer.normalize("é", Normalizer.Form.NFD);
        Map<String, Object> right = envelope("{\"quarantineId\":\"DQ-1\",\"expectedVersion\":2.00,\"corrected\":{\"amount\":1,\"name\":\"" + decomposed + "\"}}");

        assertThat(integrity.hash(left)).isEqualTo(integrity.hash(right));
    }

    @Test
    void actionTargetVersionAndPayloadMutationsChangeHash() {
        Map<String, Object> base = envelope("{\"quarantineId\":\"DQ-1\",\"expectedVersion\":2,\"corrected\":{\"name\":\"A\"}}");
        String hash = integrity.hash(base);
        for (Map.Entry<String, Object> mutation : Map.of(
                "actionType", "OTHER",
                "targetId", "DQ-2",
                "payloadSnapshot", "{\"quarantineId\":\"DQ-1\",\"expectedVersion\":3,\"corrected\":{\"name\":\"A\"}}"
        ).entrySet()) {
            Map<String, Object> changed = new LinkedHashMap<>(base);
            changed.put(mutation.getKey(), mutation.getValue());
            assertThat(integrity.hash(changed)).isNotEqualTo(hash);
        }
    }

    @Test
    void fakeStoredHashFailsVerification() {
        Map<String, Object> envelope = envelope("{\"quarantineId\":\"DQ-1\",\"expectedVersion\":2,\"corrected\":{\"name\":\"A\"}}");
        envelope.put("payloadHash", "0".repeat(64));
        assertThat(integrity.verify(envelope).valid()).isFalse();
    }

    private Map<String, Object> envelope(String payload) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("requestKey", "REQ-1");
        value.put("policyCode", "DQ-CORRECTION");
        value.put("policyVersion", 1);
        value.put("actionType", "DATA_QUALITY_CORRECTION");
        value.put("ownerModule", "CMN");
        value.put("ownerCommand", "correctQuarantine");
        value.put("targetType", "DATA_QUALITY_QUARANTINE");
        value.put("targetId", "DQ-1");
        value.put("requestedBy", "maker");
        value.put("requestReason", "approved correction");
        value.put("expireAt", "2026-08-07T00:00:00Z");
        value.put("transactionId", "TX-1");
        value.put("payloadSnapshot", payload);
        return value;
    }
    @Test
    void databaseTimestampPrecisionProducesStableHash() {
        Map<String, Object> left = envelope("{\"expectedVersion\":1,\"corrected\":{\"name\":\"Kim\"}}");
        Map<String, Object> right = new LinkedHashMap<>(left);
        left.put("expireAt", java.time.Instant.parse("2026-08-06T01:02:03.123456789Z"));
        right.put("expireAt", java.sql.Timestamp.from(java.time.Instant.parse("2026-08-06T01:02:03.123Z")));
        assertThat(integrity.hash(left)).isEqualTo(integrity.hash(right));
    }

    @Test
    void normalizedDuplicateKeysFailClosed() {
        String decomposed = Normalizer.normalize("é", Normalizer.Form.NFD);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> integrity.canonicalPayload(
                "{\"é\":1,\"" + decomposed + "\":2}"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("duplicate JSON key");
    }

    @Test
    void malformedStoredHashIsNeverReturnedToAudit() {
        Map<String, Object> envelope = envelope("{\"expectedVersion\":1,\"corrected\":{\"name\":\"Kim\"}}");
        envelope.put("payloadHash", "sensitive-untrusted-content");
        assertThat(integrity.verify(envelope).storedHash()).isEqualTo("INVALID");
    }

    @Test
    void topLevelExpectedVersionOverridesPayloadAndMustBeIntegral() {
        Map<String, Object> base = envelope("{\"quarantineId\":\"DQ-1\",\"expectedVersion\":2,\"corrected\":{\"name\":\"A\"}}");
        String payloadDerived = integrity.hash(base);
        Map<String, Object> equivalent = new LinkedHashMap<>(base);
        equivalent.put("expectedVersion", 2.0);
        assertThat(integrity.hash(equivalent)).isEqualTo(payloadDerived);

        Map<String, Object> changed = new LinkedHashMap<>(base);
        changed.put("expectedVersion", 3);
        assertThat(integrity.hash(changed)).isNotEqualTo(payloadDerived);

        Map<String, Object> fractional = new LinkedHashMap<>(base);
        fractional.put("expectedVersion", 2.5);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> integrity.hash(fractional))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("expectedVersion");
    }


}
