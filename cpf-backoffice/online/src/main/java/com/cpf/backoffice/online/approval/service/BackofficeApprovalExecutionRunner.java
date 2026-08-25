package com.cpf.backoffice.online.approval.service;

import com.cpf.backoffice.online.management.service.BackofficeManagementService;
import com.cpf.core.api.error.CpfValidationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.cpf.foundation.annotation.CpfService;

import java.util.*;

/** 승인 commit 이후 실제 Backoffice Owner 업무를 실행하고 UNKNOWN은 비파괴 조회로만 대사합니다. */
@CpfService
public class BackofficeApprovalExecutionRunner {
    private static final TypeReference<LinkedHashMap<String,Object>> MAP = new TypeReference<>() {};
    private final BackofficeApprovalExecutionStateService state;
    private final BackofficeManagementService management;
    private final ObjectMapper mapper;

    public BackofficeApprovalExecutionRunner(BackofficeApprovalExecutionStateService state,
                                             BackofficeManagementService management,
                                             ObjectMapper mapper) {
        this.state = state; this.management = management; this.mapper = mapper;
    }

    public void execute(long approvalId) {
        Optional<BackofficeApprovalExecutionStateService.ExecutionWork> claimed = state.claim(approvalId);
        if (claimed.isEmpty()) return;
        var work = claimed.get();
        try {
            Map<String,Object> payload = payload(work.document());
            Object result = executeOwner(work.ownerAction(), payload, work.operatorId(), approvalId);
            state.finish(approvalId, work.fenceToken(), "RUNNING", "SUCCEEDED",
                    "OWNER_APPLIED", summarize(result), false, work.operatorId());
        } catch (CpfValidationException | IllegalArgumentException known) {
            state.finish(approvalId, work.fenceToken(), "RUNNING", "FAILED",
                    "OWNER_REJECTED", known.getMessage(), false, work.operatorId());
        } catch (Exception uncertain) {
            state.finish(approvalId, work.fenceToken(), "RUNNING", "UNKNOWN",
                    "OWNER_RESULT_UNKNOWN", uncertain.getMessage(), true, work.operatorId());
        }
    }

    public Map<String,Object> reconcile(long approvalId, String operatorId) {
        var work = state.claimReconcile(approvalId, operatorId)
                .orElseThrow(() -> new CpfValidationException("UNKNOWN 실행결과만 Reconcile할 수 있습니다."));
        try {
            Map<String,Object> payload = payload(work.document());
            boolean applied = observeOwner(work.ownerAction(), payload);
            if (applied) {
                state.finish(approvalId, work.fenceToken(), "RECONCILING", "RECOVERED",
                        "OWNER_STATE_CONFIRMED", "승인 Snapshot과 실제 Owner 상태가 일치합니다.", false, operatorId);
            } else {
                state.finish(approvalId, work.fenceToken(), "RECONCILING", "UNKNOWN",
                        "OWNER_STATE_NOT_CONFIRMED", "실제 적용 여부를 확정할 수 없어 UNKNOWN을 유지합니다.", true, operatorId);
            }
        } catch (Exception observationFailure) {
            state.finish(approvalId, work.fenceToken(), "RECONCILING", "UNKNOWN",
                    "OWNER_RECONCILE_FAILED", observationFailure.getMessage(), true, operatorId);
        }
        return Map.of("approvalId", approvalId, "reconciled", true);
    }

    private Object executeOwner(String action, Map<String,Object> payload, String operatorId, long approvalId) {
        Map<String,Object> body = ownerPayload(payload);
        body.putIfAbsent("reason", "승인된 결재 실행 #" + approvalId);
        return switch (normalize(action)) {
            case "EMPLOYEE_SAVE", "BACKOFFICE_EMPLOYEE_SAVE" -> management.saveEmployee(
                    mapper.convertValue(body, BackofficeManagementService.EmployeeRequest.class), operatorId);
            case "ORGANIZATION_SAVE", "BACKOFFICE_ORGANIZATION_SAVE" -> management.saveOrganization(
                    mapper.convertValue(body, BackofficeManagementService.OrganizationRequest.class), operatorId);
            default -> throw new CpfValidationException("지원되지 않는 Backoffice Approval Owner Action입니다: " + action);
        };
    }

    private boolean observeOwner(String action, Map<String,Object> payload) {
        Map<String,Object> body = ownerPayload(payload);
        body.putIfAbsent("reason", "결재 UNKNOWN 비파괴 대사");
        return switch (normalize(action)) {
            case "EMPLOYEE_SAVE", "BACKOFFICE_EMPLOYEE_SAVE" -> management.matchesEmployeeSave(
                    mapper.convertValue(body, BackofficeManagementService.EmployeeRequest.class));
            case "ORGANIZATION_SAVE", "BACKOFFICE_ORGANIZATION_SAVE" -> management.matchesOrganizationSave(
                    mapper.convertValue(body, BackofficeManagementService.OrganizationRequest.class));
            default -> false;
        };
    }

    private Map<String,Object> payload(Map<String,Object> document) {
        try {
            Object parsed = mapper.readValue(Objects.toString(document.get("payloadJson"), "{}"), Object.class);
            if (!(parsed instanceof Map<?,?> map)) throw new CpfValidationException("승인 payload는 JSON Object여야 합니다.");
            LinkedHashMap<String,Object> result = new LinkedHashMap<>(); map.forEach((k,v)->result.put(String.valueOf(k),v)); return result;
        } catch (CpfValidationException e) { throw e; }
        catch (Exception e) { throw new CpfValidationException("승인 payload를 해석할 수 없습니다."); }
    }

    private static Map<String,Object> ownerPayload(Map<String,Object> payload) {
        Object after = payload.get("after");
        if (after instanceof Map<?,?> map) { LinkedHashMap<String,Object> r=new LinkedHashMap<>();map.forEach((k,v)->r.put(String.valueOf(k),v));return r; }
        return new LinkedHashMap<>(payload);
    }
    private static String normalize(String value){return Objects.toString(value,"").trim().toUpperCase(Locale.ROOT);}
    private static String summarize(Object result){String s=Objects.toString(result,"SUCCEEDED");return s.length()<=1000?s:s.substring(0,1000);}
}
