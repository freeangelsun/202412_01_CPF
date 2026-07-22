package cpf.pfw.common.workflow;

/**
 * CPF 기능 설명입니다.
 */
public final class CpfWorkflowHeaders {
    public static final String HEADER_WORKFLOW_ID = "X-Workflow-Id";
    public static final String HEADER_WORKFLOW_NAME = "X-Workflow-Name";
    public static final String HEADER_WORKFLOW_INSTANCE_ID = "X-Workflow-Instance-Id";
    public static final String HEADER_WORKFLOW_STEP_ID = "X-Workflow-Step-Id";
    public static final String HEADER_WORKFLOW_STEP_NAME = "X-Workflow-Step-Name";
    public static final String HEADER_WORKFLOW_FAILURE_POLICY = "X-Workflow-Failure-Policy";
    public static final String HEADER_COMPENSATION_YN = "X-Compensation-Yn";
    public static final String HEADER_COMPENSATION_TRANSACTION_ID = "X-Compensation-Transaction-Id";
    public static final String HEADER_COMPENSATION_TARGET_TRANSACTION_ID = "X-Compensation-Target-Transaction-Id";

    private CpfWorkflowHeaders() {
    }
}

