package com.cpf.core.common.logging;

import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.common.logging.fallback.TransactionLogFallbackStore;
import com.cpf.core.mapper.common.logging.TransactionLogMapper;
import com.cpf.core.service.common.logging.TransactionLogService;
import org.slf4j.LoggerFactory;

import java.util.Map;

/** Verifies that DB and fallback failures do not disclose credentials through standard logs. */
public final class TransactionLogListenerMaskingHarness {
    private TransactionLogListenerMaskingHarness() {}

    public static void main(String[] args) {
        TransactionLogMapper mapper = new TransactionLogMapper() {
            @Override public boolean existsRecoveryEvent(String recoveryEventId) { return false; }
            @Override public void insertTransactionLog(TransactionLogRecord record) {
                throw new IllegalStateException("password=db-secret Authorization: Bearer aaa.bbb.ccc");
            }
            @Override public void insertTransactionLogDetail(Long logIdx, String key, String value, String auditUser) {}
        };
        TransactionLogService service = new TransactionLogService(mapper);
        TransactionLogFallbackStore fallback = new TransactionLogFallbackStore() {
            @Override public boolean enqueue(
                    TransactionLogRecord record,
                    Map<String, String> details,
                    LogPolicyDecision policy,
                    Throwable failure) {
                throw new IllegalStateException("token=fallback-secret");
            }
        };
        TransactionLogListener listener = new TransactionLogListener(service, fallback);
        listener.handleTransactionLogEvent(new TransactionLogEvent(
                new TransactionLogRecord("customer-transaction-12345"), Map.of(), null));
        String rendered = LoggerFactory.captured();
        for (String leaked : java.util.List.of("db-secret", "aaa.bbb.ccc", "fallback-secret", "customer-transaction-12345")) {
            if (rendered.contains(leaked)) throw new AssertionError("secret leaked through fallback log: " + leaked);
        }
        if (!rendered.contains("***")) throw new AssertionError("masked evidence marker missing: " + rendered);
        if (!rendered.contains("sha256:")) throw new AssertionError("opaque correlation marker missing: " + rendered);
        if (CpfTransactionContextAnomalyMonitor.count() != 1L) {
            throw new AssertionError("double failure anomaly was not recorded");
        }
        System.out.println("CPF_TRANSACTION_LOG_LISTENER_MASKING_HARNESS_PASS");
    }
}
