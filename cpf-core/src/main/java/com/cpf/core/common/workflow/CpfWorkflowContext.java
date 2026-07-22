package com.cpf.core.common.workflow;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Collections;
import java.util.Map;

/**
 * 현재 HTTP 요청에 귀속된 워크플로 메타데이터를 관리합니다.
 *
 * <p>요청 헤더를 메타데이터로 변환하고 하위 HTTP 호출에 필요한 헤더만 다시
 * 내보냅니다. 데이터는 요청 범위에만 저장되어 스레드 풀 사이에서 누출되지 않습니다.</p>
 */
public final class CpfWorkflowContext {
    private static final String ATTR_WORKFLOW_METADATA = "cpfWorkflowMetadata";

    private CpfWorkflowContext() {
    }

    public static void initializeFromHeaders(HttpServletRequest request) {
        if (request == null) {
            return;
        }

        CpfWorkflowMetadata metadata = CpfWorkflowMetadata.builder()
                .workflowId(request.getHeader(CpfWorkflowHeaders.HEADER_WORKFLOW_ID))
                .workflowName(request.getHeader(CpfWorkflowHeaders.HEADER_WORKFLOW_NAME))
                .workflowInstanceId(request.getHeader(CpfWorkflowHeaders.HEADER_WORKFLOW_INSTANCE_ID))
                .workflowStepId(request.getHeader(CpfWorkflowHeaders.HEADER_WORKFLOW_STEP_ID))
                .workflowStepName(request.getHeader(CpfWorkflowHeaders.HEADER_WORKFLOW_STEP_NAME))
                .failurePolicy(CpfWorkflowFailurePolicy.from(
                        request.getHeader(CpfWorkflowHeaders.HEADER_WORKFLOW_FAILURE_POLICY)))
                .compensation("Y".equalsIgnoreCase(request.getHeader(CpfWorkflowHeaders.HEADER_COMPENSATION_YN)))
                .compensationTransactionId(request.getHeader(CpfWorkflowHeaders.HEADER_COMPENSATION_TRANSACTION_ID))
                .compensationTargetTransactionId(request.getHeader(CpfWorkflowHeaders.HEADER_COMPENSATION_TARGET_TRANSACTION_ID))
                .build();

        if (metadata.isActive()) {
            apply(metadata);
        }
    }

    public static CpfWorkflowMetadata current() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        Object value = attributes.getAttribute(ATTR_WORKFLOW_METADATA, RequestAttributes.SCOPE_REQUEST);
        return value instanceof CpfWorkflowMetadata metadata ? metadata : null;
    }

    public static void apply(CpfWorkflowMetadata metadata) {
        if (metadata == null || !metadata.isActive()) {
            return;
        }

        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            attributes.setAttribute(ATTR_WORKFLOW_METADATA, metadata, RequestAttributes.SCOPE_REQUEST);
        }
    }

    public static Map<String, String> propagationHeaders() {
        CpfWorkflowMetadata metadata = current();
        if (metadata == null || !metadata.isActive()) {
            return Collections.emptyMap();
        }

        return metadata.propagationHeaders();
    }

    public static void clear() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            attributes.removeAttribute(ATTR_WORKFLOW_METADATA, RequestAttributes.SCOPE_REQUEST);
        }
    }
}

