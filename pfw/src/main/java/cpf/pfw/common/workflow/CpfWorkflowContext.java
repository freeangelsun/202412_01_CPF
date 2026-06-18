package cpf.pfw.common.workflow;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Collections;
import java.util.Map;

/**
 * ?꾩옱 ?붿껌???뚰겕?뚮줈??硫뷀??곗씠?곕? 蹂닿??섍퀬 ?섏쐞 ?쒕퉬???몄텧 ?ㅻ뜑濡??꾪뙆?⑸땲??
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

