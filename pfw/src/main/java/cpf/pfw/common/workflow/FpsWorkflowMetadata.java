package cpf.pfw.common.workflow;

import lombok.Builder;
import lombok.Value;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ?꾩옱 ?붿껌?먯꽌 濡쒓렇? ?섏쐞 ?쒕퉬???몄텧???ъ슜???뚰겕?뚮줈??硫뷀??곗씠?곗엯?덈떎.
 */
@Value
@Builder(toBuilder = true)
public class FpsWorkflowMetadata {
    String workflowId;
    String workflowName;
    String workflowInstanceId;
    String workflowStepId;
    String workflowStepName;
    FpsWorkflowFailurePolicy failurePolicy;
    boolean compensation;
    String compensationTransactionId;
    String compensationTargetTransactionId;

    public boolean isActive() {
        return hasText(workflowId)
                || hasText(workflowName)
                || hasText(workflowInstanceId)
                || hasText(workflowStepId)
                || hasText(workflowStepName)
                || failurePolicy != null
                || compensation
                || hasText(compensationTransactionId)
                || hasText(compensationTargetTransactionId);
    }

    public Map<String, String> propagationHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        putIfHasText(headers, FpsWorkflowHeaders.HEADER_WORKFLOW_ID, workflowId);
        putIfHasText(headers, FpsWorkflowHeaders.HEADER_WORKFLOW_NAME, workflowName);
        putIfHasText(headers, FpsWorkflowHeaders.HEADER_WORKFLOW_INSTANCE_ID, workflowInstanceId);
        putIfHasText(headers, FpsWorkflowHeaders.HEADER_WORKFLOW_STEP_ID, workflowStepId);
        putIfHasText(headers, FpsWorkflowHeaders.HEADER_WORKFLOW_STEP_NAME, workflowStepName);
        if (failurePolicy != null) {
            headers.put(FpsWorkflowHeaders.HEADER_WORKFLOW_FAILURE_POLICY, failurePolicy.name());
        }
        if (compensation) {
            headers.put(FpsWorkflowHeaders.HEADER_COMPENSATION_YN, "Y");
        }
        putIfHasText(headers, FpsWorkflowHeaders.HEADER_COMPENSATION_TRANSACTION_ID, compensationTransactionId);
        putIfHasText(headers, FpsWorkflowHeaders.HEADER_COMPENSATION_TARGET_TRANSACTION_ID, compensationTargetTransactionId);
        return headers;
    }

    private static void putIfHasText(Map<String, String> headers, String name, String value) {
        if (hasText(value)) {
            headers.put(name, value);
        }
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}

