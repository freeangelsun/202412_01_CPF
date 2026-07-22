package com.cpf.core.common.workflow;

/** 워크플로 및 보상 처리의 운영 추적 상태입니다. */
public enum CpfWorkflowStatus {
    NONE,
    STARTED,
    COMPLETED,
    FAILED,
    RETRY_PENDING,
    COMPENSATING,
    COMPENSATED,
    COMPENSATION_FAILED,
    PENDING,
    VERIFY_REQUIRED,
    MANUAL_REQUIRED,
    IGNORED
}

