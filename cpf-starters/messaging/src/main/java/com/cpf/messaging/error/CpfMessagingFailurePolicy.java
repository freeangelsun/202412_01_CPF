package com.cpf.messaging.error;

import com.cpf.core.api.error.CpfErrorDefinition;

/** Messaging Owner의 retry/DLQ 판정입니다. */
public final class CpfMessagingFailurePolicy {
    private CpfMessagingFailurePolicy() { }

    public static Decision map(CpfErrorDefinition error, int attempt, int maxAttempts) {
        if (error == null) return new Decision(false, true);
        boolean retry = error.retryable() && attempt < maxAttempts;
        return new Decision(retry,
                !retry && error.category() != CpfErrorDefinition.Category.VALIDATION);
    }

    public record Decision(boolean retry, boolean dlq) { }
}
