package cpf.pfw.common.workflow;

/**
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
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

