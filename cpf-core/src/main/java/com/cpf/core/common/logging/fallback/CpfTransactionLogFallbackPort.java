package com.cpf.core.common.logging.fallback;

import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.common.logging.TransactionLogRecord;

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
