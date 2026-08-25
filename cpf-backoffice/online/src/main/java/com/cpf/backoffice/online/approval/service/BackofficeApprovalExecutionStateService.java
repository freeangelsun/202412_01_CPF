package com.cpf.backoffice.online.approval.service;

import com.cpf.backoffice.online.approval.repository.BackofficeApprovalPolicyRepository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.cpf.foundation.annotation.CpfService;

import java.util.*;

/** MBW Approval 실행 상태를 짧은 REQUIRES_NEW transaction으로 fencing 합니다. */
@CpfService
public class BackofficeApprovalExecutionStateService {
    private final BackofficeApprovalPolicyRepository repository;

    public BackofficeApprovalExecutionStateService(BackofficeApprovalPolicyRepository repository) {
        this.repository = repository;
    }

    @Transactional(transactionManager="MBW_TRANSACTION_MANAGER", propagation=Propagation.REQUIRES_NEW)
    public Optional<ExecutionWork> claim(long approvalId) {
        Map<String,Object> execution = repository.findExecution(approvalId).orElse(null);
        if (execution == null || !"PENDING".equals(text(execution,"executionStatus"))) return Optional.empty();
        long fence = number(execution,"fenceToken");
        String operator = text(execution,"approvedBy");
        if (repository.claimExecution(approvalId, fence, operator) != 1) return Optional.empty();
        Map<String,Object> claimed = repository.findExecution(approvalId).orElseThrow();
        Map<String,Object> document = repository.findDocument(approvalId).orElseThrow();
        return Optional.of(new ExecutionWork(approvalId, text(claimed,"ownerAction"),
                number(claimed,"fenceToken"), operator, document));
    }

    @Transactional(transactionManager="MBW_TRANSACTION_MANAGER", propagation=Propagation.REQUIRES_NEW)
    public Optional<ExecutionWork> claimReconcile(long approvalId, String operatorId) {
        Map<String,Object> execution = repository.findExecution(approvalId).orElse(null);
        if (execution == null || !"UNKNOWN".equals(text(execution,"executionStatus"))) return Optional.empty();
        long fence = number(execution,"fenceToken");
        if (repository.startReconcile(approvalId, fence, operatorId) != 1) return Optional.empty();
        Map<String,Object> claimed = repository.findExecution(approvalId).orElseThrow();
        Map<String,Object> document = repository.findDocument(approvalId).orElseThrow();
        return Optional.of(new ExecutionWork(approvalId, text(claimed,"ownerAction"),
                number(claimed,"fenceToken"), operatorId, document));
    }

    @Transactional(transactionManager="MBW_TRANSACTION_MANAGER", propagation=Propagation.REQUIRES_NEW)
    public void finish(long approvalId, long fenceToken, String expectedStatus, String status,
                       String code, String message, boolean recoveryRequired, String operatorId) {
        if (repository.completeExecution(approvalId, fenceToken, expectedStatus, status,
                code, safe(message), recoveryRequired, operatorId) != 1) {
            throw new IllegalStateException("MBW_APPROVAL_EXECUTION_FENCE_CONFLICT approvalId=" + approvalId);
        }
    }

    private static String safe(String value) {
        if (value == null) return null;
        String text = value.replaceAll("(?i)(password|secret|token|credential|api.?key)\\s*[=:]\\s*[^,;\\s]+", "$1=***");
        return text.length() <= 1000 ? text : text.substring(0,1000);
    }
    private static String text(Map<String,Object> row,String key){Object v=row.get(key);if(v==null)v=row.get(key.toUpperCase(Locale.ROOT));return Objects.toString(v,"");}
    private static long number(Map<String,Object> row,String key){Object v=row.get(key);if(v==null)v=row.get(key.toUpperCase(Locale.ROOT));return v instanceof Number n?n.longValue():Long.parseLong(String.valueOf(v));}

    public record ExecutionWork(long approvalId, String ownerAction, long fenceToken,
                                String operatorId, Map<String,Object> document) {}
}
