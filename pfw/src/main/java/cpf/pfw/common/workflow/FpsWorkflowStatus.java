package cpf.pfw.common.workflow;

/**
 * 遺꾩궛 嫄곕옒/?뚰겕?뚮줈??愿?먯쓽 泥섎━ ?곹깭?낅땲??
 * pfw_transaction_log ⑸줉 ?붾㈃怨?pfw_transaction_log_detail ?곸꽭 ?붾㈃?먯꽌 ?숈씪??媛믪쑝濡?議고쉶?⑸땲??
 */
public enum FpsWorkflowStatus {
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

