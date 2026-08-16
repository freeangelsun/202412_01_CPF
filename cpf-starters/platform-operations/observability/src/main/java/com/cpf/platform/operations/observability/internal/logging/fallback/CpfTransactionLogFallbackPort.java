package com.cpf.platform.operations.observability.internal.logging.fallback;

import com.cpf.platform.operations.observability.api.logging.policy.LogPolicyDecision;
import com.cpf.platform.operations.observability.spi.logging.TransactionLogRecord;

import java.util.Map;

/** Minimal durable fallback boundary used by the asynchronous DB log writer. */
@FunctionalInterface
public interface CpfTransactionLogFallbackPort {
    boolean enqueue(
            TransactionLogRecord record,
            Map<String, String> details,
            LogPolicyDecision logPolicy,
            Throwable failure);
}
