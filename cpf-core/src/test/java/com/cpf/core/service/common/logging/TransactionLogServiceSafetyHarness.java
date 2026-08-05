package com.cpf.core.service.common.logging;

import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.api.logging.policy.LogPolicyTargetType;
import com.cpf.core.common.logging.TransactionLogRecord;
import com.cpf.core.mapper.common.logging.TransactionLogMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Verifies immutable detail maps, policy filtering, and detail masking. */
public final class TransactionLogServiceSafetyHarness {
    private TransactionLogServiceSafetyHarness() {}

    public static void main(String[] args) {
        CapturingMapper mapper = new CapturingMapper();
        TransactionLogService service = new TransactionLogService(mapper);
        TransactionLogRecord record = new TransactionLogRecord();
        record.setLogIdx(7L);
        record.setExecUser("tester");
        record.setRequestBody("password=record-secret");
        record.setResponse("token=record-response-secret");
        record.setInternalMessage("privateKey=record-stack-secret");
        Map<String, String> immutableDetails = Map.of(
                "requestBody", "password=request-secret",
                "response", "token=response-secret",
                "error.internalMessage", "privateKey=stack-secret",
                "safe", "Authorization: Bearer aaa.bbb.ccc");
        LogPolicyDecision policy = LogPolicyDecision.cpfDefault(LogPolicyTargetType.MODULE, "CORE");

        service.saveTransactionLog(record, immutableDetails, policy);
        if (mapper.summaryCount != 1) throw new AssertionError("summary was not inserted");
        if (record.getRequestBody() != null || record.getResponse() != null || record.getInternalMessage() != null) {
            throw new AssertionError("record payload policy was not applied");
        }
        if (immutableDetails.size() != 4) throw new AssertionError("caller-owned immutable map changed");
        if (!mapper.details.keySet().equals(java.util.Set.of("safe"))) {
            throw new AssertionError("disallowed detail fields persisted: " + mapper.details.keySet());
        }
        String safe = mapper.details.get("safe");
        if (safe == null || safe.contains("aaa.bbb.ccc")) {
            throw new AssertionError("authorization detail was not masked: " + safe);
        }

        DuplicateRaceMapper duplicateRace = new DuplicateRaceMapper(true);
        TransactionLogRecord duplicateRecord = record(8L);
        new TransactionLogService(duplicateRace).saveTransactionLog(duplicateRecord, Map.of(), policy);
        if (duplicateRace.insertAttempts != 1 || duplicateRace.existsChecks != 2) {
            throw new AssertionError("recovery duplicate race did not converge idempotently");
        }

        DuplicateRaceMapper unrelatedViolation = new DuplicateRaceMapper(false);
        try {
            new TransactionLogService(unrelatedViolation).saveTransactionLog(record(9L), Map.of(), policy);
            throw new AssertionError("unrelated integrity failure must propagate");
        } catch (org.springframework.dao.DataIntegrityViolationException expected) {
            // expected
        }
        System.out.println("CPF_TRANSACTION_LOG_SERVICE_SAFETY_HARNESS_PASS");
    }

    private static TransactionLogRecord record(long logIdx) {
        TransactionLogRecord record = new TransactionLogRecord();
        record.setLogIdx(logIdx);
        record.setExecUser("tester");
        return record;
    }

    private static final class DuplicateRaceMapper implements TransactionLogMapper {
        private final boolean appearsAfterConflict;
        private int existsChecks;
        private int insertAttempts;

        private DuplicateRaceMapper(boolean appearsAfterConflict) {
            this.appearsAfterConflict = appearsAfterConflict;
        }

        @Override public boolean existsRecoveryEvent(String recoveryEventId) {
            existsChecks++;
            return appearsAfterConflict && insertAttempts > 0;
        }
        @Override public void insertTransactionLog(TransactionLogRecord record) {
            insertAttempts++;
            throw new org.springframework.dao.DataIntegrityViolationException("duplicate");
        }
        @Override public void insertTransactionLogDetail(
                Long logIdx, String key, String value, String auditUser) {}
    }

    private static final class CapturingMapper implements TransactionLogMapper {
        int summaryCount;
        final Map<String, String> details = new LinkedHashMap<>();
        @Override public boolean existsRecoveryEvent(String recoveryEventId) { return false; }
        @Override public void insertTransactionLog(TransactionLogRecord record) { summaryCount++; }
        @Override public void insertTransactionLogDetail(Long logIdx, String key, String value, String auditUser) {
            details.put(key, value);
        }
    }
}
