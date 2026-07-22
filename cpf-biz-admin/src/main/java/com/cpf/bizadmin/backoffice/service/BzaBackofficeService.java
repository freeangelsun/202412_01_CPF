package com.cpf.bizadmin.backoffice.service;

import com.cpf.bizadmin.backoffice.repository.BzaBackofficeRepository;
import com.cpf.common.utils.TextUtils;
import com.cpf.core.common.exception.CpfNotFoundException;
import com.cpf.core.common.exception.CpfValidationException;
import com.cpf.core.common.logging.TransactionContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** BZA 조직·직원·실효 권한·결재 상태 전이를 담당합니다. */
@Service
public class BzaBackofficeService extends com.cpf.bizadmin.common.base.BzaBaseService {
    private static final Set<String> APPROVAL_ACTIONS = Set.of(
            "SUBMIT", "APPROVE", "AGREE", "REJECT", "WITHDRAW", "CANCEL", "RESUBMIT");

    private final BzaBackofficeRepository repository;

    public BzaBackofficeService(BzaBackofficeRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> findOrganizations() {
        return repository.findOrganizations();
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> saveOrganization(OrganizationRequest request, String operatorId) {
        String code = required(request.organizationCode(), "organizationCode").toUpperCase(Locale.ROOT);
        String user = required(operatorId, "operatorId");
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("organizationCode", code);
        values.put("parentOrganizationCode", blankToNull(request.parentOrganizationCode()));
        values.put("organizationName", required(request.organizationName(), "organizationName"));
        values.put("organizationType", defaultText(request.organizationType(), "DEPARTMENT"));
        values.put("sortOrder", request.sortOrder() == null ? 0 : request.sortOrder());
        values.put("useYn", yn(request.useYn(), "Y"));
        values.put("requestUser", user);
        repository.saveOrganization(values);
        audit(user, "ORGANIZATION_SAVE", "bza_organization", code,
                required(request.reason(), "reason"), null, values);
        return values;
    }

    public List<Map<String, Object>> findEmployees(String organizationCode, String status) {
        return repository.findEmployees(blankToNull(organizationCode), blankToNull(status));
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> saveEmployee(EmployeeRequest request, String operatorId) {
        String employeeNo = required(request.employeeNo(), "employeeNo").toUpperCase(Locale.ROOT);
        String user = required(operatorId, "operatorId");
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("employeeNo", employeeNo);
        values.put("adminUserId", request.adminUserId());
        values.put("organizationCode", required(request.organizationCode(), "organizationCode").toUpperCase(Locale.ROOT));
        values.put("employeeName", required(request.employeeName(), "employeeName"));
        values.put("positionCode", blankToNull(request.positionCode()));
        values.put("jobTitleCode", blankToNull(request.jobTitleCode()));
        values.put("managerEmployeeNo", blankToNull(request.managerEmployeeNo()));
        values.put("employmentStatus", defaultText(request.employmentStatus(), "ACTIVE"));
        values.put("joinDate", request.joinDate());
        values.put("leaveDate", request.leaveDate());
        values.put("email", blankToNull(request.email()));
        values.put("mobileNo", blankToNull(request.mobileNo()));
        values.put("delegatedApproverNo", blankToNull(request.delegatedApproverNo()));
        values.put("absenceFrom", request.absenceFrom());
        values.put("absenceTo", request.absenceTo());
        values.put("useYn", yn(request.useYn(), "Y"));
        values.put("requestUser", user);
        repository.saveEmployee(values);
        audit(user, "EMPLOYEE_SAVE", "bza_employee", employeeNo,
                required(request.reason(), "reason"), null, values);
        return values;
    }

    public List<Map<String, Object>> findEffectivePermissions(String loginId) {
        return repository.findEffectivePermissions(required(loginId, "loginId"));
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> createApproval(ApprovalCreateRequest request, String operatorId) {
        String requester = required(request.requesterEmployeeNo(), "requesterEmployeeNo");
        String user = required(operatorId, "operatorId");
        List<ApprovalLineRequest> lines = request.lines() == null ? List.of() : request.lines();
        if (lines.isEmpty()) {
            throw new CpfValidationException("결재선은 한 명 이상이어야 합니다.");
        }
        if (lines.stream().anyMatch(line -> requester.equalsIgnoreCase(line.approverEmployeeNo()))) {
            throw new CpfValidationException("요청자와 결재자는 같을 수 없습니다.");
        }
        String approvalNo = "BZA-" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("approvalNo", approvalNo);
        values.put("approvalType", required(request.approvalType(), "approvalType"));
        values.put("businessDomain", required(request.businessDomain(), "businessDomain"));
        values.put("title", required(request.title(), "title"));
        values.put("requesterEmployeeNo", requester);
        values.put("approvalMode", defaultText(request.approvalMode(), "SEQUENTIAL"));
        values.put("dueAt", request.dueAt());
        values.put("payloadJson", blankToNull(request.payloadJson()));
        values.put("attachmentGroupId", blankToNull(request.attachmentGroupId()));
        values.put("transactionGlobalId", TransactionContext.getOrCreateTransactionId());
        values.put("requestUser", user);
        long approvalId = repository.createApproval(values);
        for (ApprovalLineRequest line : lines) {
            int stepNo = line.stepNo() == null ? 1 : line.stepNo();
            if (stepNo < 1) {
                throw new CpfValidationException("결재 단계는 1 이상이어야 합니다.");
            }
            repository.addApprovalLine(
                    approvalId,
                    stepNo,
                    required(line.approverEmployeeNo(), "approverEmployeeNo"),
                    defaultText(line.decisionRule(), "ALL_APPROVE"),
                    user);
        }
        audit(user, "APPROVAL_CREATE", "bza_approval_document", String.valueOf(approvalId),
                required(request.reason(), "reason"), null, values);
        return findApproval(approvalId);
    }

    public List<Map<String, Object>> findApprovals(String status, String employeeNo, int limit) {
        return repository.findApprovals(blankToNull(status), blankToNull(employeeNo), Math.max(1, Math.min(limit, 500)));
    }

    public Map<String, Object> findApproval(long approvalId) {
        Map<String, Object> document = new LinkedHashMap<>(repository.findApproval(approvalId)
                .orElseThrow(() -> new CpfNotFoundException("결재 문서를 찾을 수 없습니다. approvalId=" + approvalId)));
        document.put("lines", repository.findApprovalLines(approvalId));
        document.put("history", repository.findApprovalHistory(approvalId));
        return document;
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> act(long approvalId, ApprovalActionRequest request, String operatorId) {
        String action = required(request.action(), "action").toUpperCase(Locale.ROOT);
        if (!APPROVAL_ACTIONS.contains(action)) {
            throw new CpfValidationException("지원하지 않는 결재 행위입니다. action=" + action);
        }
        String operator = required(operatorId, "operatorId");
        String actor = repository.findEmployeeNoByLoginId(operator)
                .orElseThrow(() -> new CpfValidationException(
                        "결재 처리 운영자와 연결된 직원 프로필이 없습니다. operatorId=" + operator));
        String idempotencyKey = required(request.idempotencyKey(), "idempotencyKey");
        String reason = required(request.reason(), "reason");
        if (repository.approvalActionExists(idempotencyKey)) {
            return findApproval(approvalId);
        }

        Map<String, Object> before = repository.findApproval(approvalId)
                .orElseThrow(() -> new CpfNotFoundException("결재 문서를 찾을 수 없습니다. approvalId=" + approvalId));
        String beforeStatus = string(before, "approvalStatus");
        long version = number(before, "versionNo").longValue();
        int currentStep = number(before, "currentStepNo").intValue();
        String requester = string(before, "requesterEmployeeNo");
        Transition transition = transition(action, beforeStatus, actor, requester, approvalId, currentStep, request.comment());
        int updated = repository.updateApprovalStatus(
                approvalId, version, transition.afterStatus(), transition.currentStep(), operator);
        if (updated != 1) {
            throw new CpfValidationException("결재 상태가 동시에 변경되었습니다. 최신 상태를 다시 조회하세요.");
        }
        Map<String, Object> history = new LinkedHashMap<>();
        history.put("approvalId", approvalId);
        history.put("actionType", action);
        history.put("actorEmployeeNo", actor);
        history.put("idempotencyKey", idempotencyKey);
        history.put("reason", reason);
        history.put("beforeStatus", beforeStatus);
        history.put("afterStatus", transition.afterStatus());
        history.put("comment", blankToNull(request.comment()));
        history.put("transactionGlobalId", TransactionContext.getOrCreateTransactionId());
        repository.insertApprovalHistory(history);
        audit(operator, "APPROVAL_" + action, "bza_approval_document", String.valueOf(approvalId),
                reason, before, history);
        return findApproval(approvalId);
    }

    public List<Map<String, Object>> findAudits(int limit) {
        return repository.findBusinessAudits(Math.max(1, Math.min(limit, 500)));
    }

    private Transition transition(
            String action,
            String status,
            String actor,
            String requester,
            long approvalId,
            int currentStep,
            String comment) {
        return switch (action) {
            case "SUBMIT" -> {
                requireStatus(status, Set.of("DRAFT"), action);
                yield new Transition("IN_REVIEW", 1);
            }
            case "RESUBMIT" -> {
                requireStatus(status, Set.of("REJECTED", "WITHDRAWN"), action);
                yield new Transition("IN_REVIEW", 1);
            }
            case "APPROVE", "AGREE" -> {
                requireStatus(status, Set.of("IN_REVIEW"), action);
                if (actor.equalsIgnoreCase(requester)) {
                    throw new CpfValidationException("요청자는 자신의 결재 문서를 승인할 수 없습니다.");
                }
                int changed = repository.decideLine(approvalId, currentStep, actor, "APPROVED", blankToNull(comment));
                if (changed != 1) {
                    throw new CpfValidationException("현재 단계의 결재 대상자가 아니거나 이미 처리된 결재입니다.");
                }
                if (repository.countWaitingAtStep(approvalId, currentStep) > 0) {
                    yield new Transition("IN_REVIEW", currentStep);
                }
                Integer nextStep = repository.nextStep(approvalId, currentStep);
                yield nextStep == null
                        ? new Transition("APPROVED", currentStep)
                        : new Transition("IN_REVIEW", nextStep);
            }
            case "REJECT" -> {
                requireStatus(status, Set.of("IN_REVIEW"), action);
                int changed = repository.decideLine(approvalId, currentStep, actor, "REJECTED", blankToNull(comment));
                if (changed != 1) {
                    throw new CpfValidationException("현재 단계의 결재 대상자가 아니거나 이미 처리된 결재입니다.");
                }
                yield new Transition("REJECTED", currentStep);
            }
            case "WITHDRAW" -> {
                requireRequester(actor, requester);
                requireStatus(status, Set.of("IN_REVIEW"), action);
                yield new Transition("WITHDRAWN", currentStep);
            }
            case "CANCEL" -> {
                requireRequester(actor, requester);
                requireStatus(status, Set.of("DRAFT", "REJECTED", "WITHDRAWN"), action);
                yield new Transition("CANCELED", currentStep);
            }
            default -> throw new CpfValidationException("지원하지 않는 결재 행위입니다. action=" + action);
        };
    }

    private void requireStatus(String status, Set<String> allowed, String action) {
        if (!allowed.contains(status)) {
            throw new CpfValidationException("현재 상태에서는 결재 행위를 수행할 수 없습니다. status=" + status + ", action=" + action);
        }
    }

    private void requireRequester(String actor, String requester) {
        if (!actor.equalsIgnoreCase(requester)) {
            throw new CpfValidationException("결재 요청자만 이 행위를 수행할 수 있습니다.");
        }
    }

    private void audit(
            String actor,
            String action,
            String targetType,
            String targetId,
            String reason,
            Object before,
            Object after) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("transactionGlobalId", TransactionContext.getOrCreateTransactionId());
        values.put("actorId", actor);
        values.put("actionType", action);
        values.put("targetType", targetType);
        values.put("targetId", targetId);
        values.put("reason", reason);
        values.put("beforeData", before == null ? null : String.valueOf(before));
        values.put("afterData", after == null ? null : String.valueOf(after));
        repository.insertBusinessAudit(values);
    }

    private String string(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            value = row.get(key.toUpperCase(Locale.ROOT));
        }
        return value == null ? null : String.valueOf(value);
    }

    private Number number(Map<String, Object> row, String key) {
        Object value = row.get(key);
        if (value == null) {
            value = row.get(key.toUpperCase(Locale.ROOT));
        }
        return value instanceof Number number ? number : Long.parseLong(String.valueOf(value));
    }

    private String required(String value, String field) {
        return TextUtils.requireText(value, field);
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String yn(String value, String fallback) {
        String resolved = defaultText(value, fallback).toUpperCase(Locale.ROOT);
        if (!Set.of("Y", "N").contains(resolved)) {
            throw new CpfValidationException("Y/N 값이 올바르지 않습니다. value=" + value);
        }
        return resolved;
    }

    private record Transition(String afterStatus, int currentStep) {
    }

    public record OrganizationRequest(
            String organizationCode,
            String parentOrganizationCode,
            String organizationName,
            String organizationType,
            Integer sortOrder,
            String useYn,
            String requestUser,
            String reason) {
    }

    public record EmployeeRequest(
            String employeeNo,
            Long adminUserId,
            String organizationCode,
            String employeeName,
            String positionCode,
            String jobTitleCode,
            String managerEmployeeNo,
            String employmentStatus,
            LocalDate joinDate,
            LocalDate leaveDate,
            String email,
            String mobileNo,
            String delegatedApproverNo,
            LocalDate absenceFrom,
            LocalDate absenceTo,
            String useYn,
            String requestUser,
            String reason) {
    }

    public record ApprovalLineRequest(Integer stepNo, String approverEmployeeNo, String decisionRule) {
    }

    public record ApprovalCreateRequest(
            String approvalType,
            String businessDomain,
            String title,
            String requesterEmployeeNo,
            String approvalMode,
            LocalDateTime dueAt,
            String payloadJson,
            String attachmentGroupId,
            List<ApprovalLineRequest> lines,
            String requestUser,
            String reason) {
    }

    public record ApprovalActionRequest(
            String action,
            String actorEmployeeNo,
            String idempotencyKey,
            String reason,
            String comment) {
    }
}
