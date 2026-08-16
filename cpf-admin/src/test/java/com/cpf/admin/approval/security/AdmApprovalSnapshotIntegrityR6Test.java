package com.cpf.admin.approval.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class AdmApprovalSnapshotIntegrityR6Test {
    private final AdmApprovalSnapshotIntegrity integrity = new AdmApprovalSnapshotIntegrity(new ObjectMapper());

    @Test void rejectsExactAndUnicodeDuplicateKeys() {
        assertThrows(IllegalArgumentException.class, () -> integrity.canonicalPayload("{\"a\":1,\"a\":2}"));
        assertThrows(IllegalArgumentException.class, () -> integrity.canonicalPayload("{\"é\":1,\"e\\u0301\":2}"));
    }

    @Test void preservesIntegerAndDecimalCanonicalPrecision() {
        String canonical = integrity.canonicalPayload("{\"integer\":9007199254740993,\"amount\":0.12345678901234567890}");
        assertTrue(canonical.contains("9007199254740993"));
        assertTrue(canonical.contains("0.1234567890123456789"));
        assertEquals(canonical, integrity.canonicalPayload(canonical));
    }

    @Test void hashesNullPayloadFieldsWithoutDroppingThem() {
        Map<String,Object> envelope = new LinkedHashMap<>();
        envelope.put("actionType","DATA_QUALITY_CORRECTION"); envelope.put("ownerModule","ADM");
        envelope.put("ownerCommand","DATA_QUALITY_CORRECT"); envelope.put("targetType","DATA_QUALITY_QUARANTINE");
        envelope.put("targetId","DQ-1"); envelope.put("requestKey","request-001");
        envelope.put("requestedBy","requester"); envelope.put("requestReason","eight chars reason");
        envelope.put("policyCode","DQ"); envelope.put("policyVersion",1);
        envelope.put("expireAt",null); envelope.put("transactionId","tx");
        envelope.put("payloadSnapshot","{\"corrected\":{\"nullable\":null},\"expectedVersion\":1}");
        assertTrue(integrity.hash(envelope).matches("[0-9a-f]{64}"));
    }
}
