package fps.pfw.common.workflow;

/**
 * 주제영역 간 워크플로우 추적을 위해 전파하는 선택 헤더입니다.
 */
public final class FpsWorkflowHeaders {
    public static final String HEADER_WORKFLOW_ID = "X-Workflow-Id";
    public static final String HEADER_WORKFLOW_NAME = "X-Workflow-Name";
    public static final String HEADER_WORKFLOW_INSTANCE_ID = "X-Workflow-Instance-Id";
    public static final String HEADER_WORKFLOW_STEP_ID = "X-Workflow-Step-Id";
    public static final String HEADER_WORKFLOW_STEP_NAME = "X-Workflow-Step-Name";
    public static final String HEADER_WORKFLOW_FAILURE_POLICY = "X-Workflow-Failure-Policy";
    public static final String HEADER_COMPENSATION_YN = "X-Compensation-Yn";
    public static final String HEADER_COMPENSATION_TRANSACTION_ID = "X-Compensation-Transaction-Id";
    public static final String HEADER_COMPENSATION_TARGET_TRANSACTION_ID = "X-Compensation-Target-Transaction-Id";

    private FpsWorkflowHeaders() {
    }
}
