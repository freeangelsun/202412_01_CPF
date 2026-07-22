package com.cpf.core.common.workflow;

import lombok.Builder;
import lombok.Value;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 워크플로 실행과 보상 거래를 추적하기 위한 요청 단위 메타데이터입니다.
 *
 * <p>값이 존재하는 항목만 전파 헤더로 변환하여 기존 API와의 하위 호환성을 유지합니다.</p>
 */
@Value
@Builder(toBuilder = true)
public class CpfWorkflowMetadata {
    String workflowId;
    String workflowName;
    String workflowInstanceId;
    String workflowStepId;
    String workflowStepName;
    CpfWorkflowFailurePolicy failurePolicy;
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
        putIfHasText(headers, CpfWorkflowHeaders.HEADER_WORKFLOW_ID, workflowId);
        putIfHasText(headers, CpfWorkflowHeaders.HEADER_WORKFLOW_NAME, workflowName);
        putIfHasText(headers, CpfWorkflowHeaders.HEADER_WORKFLOW_INSTANCE_ID, workflowInstanceId);
        putIfHasText(headers, CpfWorkflowHeaders.HEADER_WORKFLOW_STEP_ID, workflowStepId);
        putIfHasText(headers, CpfWorkflowHeaders.HEADER_WORKFLOW_STEP_NAME, workflowStepName);
        if (failurePolicy != null) {
            headers.put(CpfWorkflowHeaders.HEADER_WORKFLOW_FAILURE_POLICY, failurePolicy.name());
        }
        if (compensation) {
            headers.put(CpfWorkflowHeaders.HEADER_COMPENSATION_YN, "Y");
        }
        putIfHasText(headers, CpfWorkflowHeaders.HEADER_COMPENSATION_TRANSACTION_ID, compensationTransactionId);
        putIfHasText(headers, CpfWorkflowHeaders.HEADER_COMPENSATION_TARGET_TRANSACTION_ID, compensationTargetTransactionId);
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

