package cpf.pfw.common.workflow;

/**
 * 二쇱젣?곸뿭 媛??뚰겕?뚮줈??異붿쟻???꾪빐 ?꾪뙆?섎뒗 ?좏깮 ?ㅻ뜑?낅땲??
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

