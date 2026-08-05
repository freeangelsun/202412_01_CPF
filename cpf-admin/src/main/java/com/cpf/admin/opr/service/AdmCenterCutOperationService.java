package com.cpf.admin.opr.service;

import com.cpf.admin.approval.service.AdmApprovalService;
import com.cpf.core.api.batch.CpfCenterCutOperationsPort;
import com.cpf.core.api.error.CpfValidationException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

/**
 * ADM Center-Cut 조회와 승인 실행 경계를 제공합니다.
 *
 * <p>조회는 BAT 운영 Port를 사용하고, 위험조치 실행은 반드시 Canonical
 * {@link AdmApprovalService}를 통과합니다. 이 Service는 별도 실행 원장이나
 * Owner 호출을 소유하지 않습니다.</p>
 */
@Service
public class AdmCenterCutOperationService extends com.cpf.admin.common.base.AdmBaseService {
    private final CpfCenterCutOperationsPort port;
    private final AdmApprovalService approvalService;

    public AdmCenterCutOperationService(
            CpfCenterCutOperationsPort port,
            AdmApprovalService approvalService) {
        this.port = port;
        this.approvalService = approvalService;
    }

    public List<Map<String, Object>> findJobs() { return port.findJobs(); }
    public Map<String, Object> findJobDetail(String id) { return port.findJobDetail(id); }
    public List<Map<String, Object>> findParameters(String id) { return port.findParameters(id); }
    public Map<String, Object> findSummary(String id) { return port.findSummary(id); }
    public List<Map<String, Object>> findTargets(String id, String status, int limit) {
        return port.findTargets(id, status, limit);
    }
    public List<Map<String, Object>> findResults(String id, String status, int limit) {
        return port.findResults(id, status, limit);
    }
    public Map<String, Object> findResultDetail(String id) { return port.findResultDetail(id); }

    public Map<String, Object> reprocessFailed(
            String executionId, long approvalRequestId, String requestKey,
            String reason, String operatorId) {
        return executeApproved(
                executionId, approvalRequestId, requestKey, reason, operatorId,
                "reprocessCenterCutFailed", "CENTER_CUT_REPROCESS_FAILED");
    }

    public Map<String, Object> reconcileUnknown(
            String executionId, long approvalRequestId, String requestKey,
            String reason, String operatorId) {
        return executeApproved(
                executionId, approvalRequestId, requestKey, reason, operatorId,
                "reconcileCenterCutUnknown", "CENTER_CUT_RECONCILE_UNKNOWN");
    }

    private Map<String, Object> executeApproved(
            String executionId, long approvalRequestId, String requestKey,
            String reason, String operatorId, String ownerCommand, String actionType) {
        String safeExecutionId = required(executionId, "executionId");
        String safeRequestKey = required(requestKey, "idempotencyKey");
        if (approvalRequestId <= 0) {
            throw new CpfValidationException("approvalRequestId는 양수여야 합니다.");
        }
        Map<String,Object> approval = approvalService.detail(approvalRequestId);
        requireMatch(approval, "requestKey", safeRequestKey);
        requireMatchIgnoreCase(approval, "ownerModule", "BAT");
        requireMatch(approval, "ownerCommand", ownerCommand);
        requireMatchIgnoreCase(approval, "targetType", "center_cut_execution");
        requireMatch(approval, "targetId", safeExecutionId);
        requireMatchIgnoreCase(approval, "actionType", actionType);
        return approvalService.execute(approvalRequestId, required(reason, "reason"), required(operatorId, "operatorId"));
    }

    private static void requireMatch(Map<String,Object> row, String field, String expected) {
        String actual = text(row, field);
        if (!expected.equals(actual)) {
            throw new CpfValidationException("승인 요청의 " + field + "가 Center-Cut 실행 요청과 일치하지 않습니다.");
        }
    }

    private static void requireMatchIgnoreCase(Map<String,Object> row, String field, String expected) {
        String actual = text(row, field);
        if (!expected.equalsIgnoreCase(actual)) {
            throw new CpfValidationException("승인 요청의 " + field + "가 Center-Cut 실행 요청과 일치하지 않습니다.");
        }
    }

    private static String text(Map<String,Object> row, String field) {
        Object value = row.get(field);
        if (value == null) value = row.get(field.toUpperCase(java.util.Locale.ROOT));
        return required(value == null ? null : String.valueOf(value), field);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new CpfValidationException(field + "는 필수입니다.");
        }
        return value.trim();
    }
}
