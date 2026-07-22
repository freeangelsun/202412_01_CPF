package com.cpf.core.common.workflow;

import java.util.Arrays;

/** 워크플로 단계 실패 후 오케스트레이터가 취할 표준 처리 정책입니다. */
public enum CpfWorkflowFailurePolicy {
    FAIL,
    RETRY,
    COMPENSATE,
    PENDING,
    VERIFY,
    MANUAL,
    IGNORE;

    public static CpfWorkflowFailurePolicy from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return Arrays.stream(values())
                .filter(policy -> policy.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(null);
    }
}

