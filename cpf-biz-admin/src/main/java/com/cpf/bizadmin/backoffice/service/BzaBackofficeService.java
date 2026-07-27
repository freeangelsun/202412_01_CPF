package com.cpf.bizadmin.backoffice.service;

import com.cpf.bizadmin.backoffice.repository.BzaBackofficeRepository;
import com.cpf.bizadmin.audit.service.BzaBusinessAuditService;
import com.cpf.core.api.util.CpfStrings;
import com.cpf.core.api.page.CpfPage;
import com.cpf.core.api.page.CpfPageRequest;
import com.cpf.core.api.error.CpfNotFoundException;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.logging.CpfTransactionContext;
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
    private final BzaBusinessAuditService auditService;

    public BzaBackofficeService(BzaBackofficeRepository repository, BzaBusinessAuditService auditService) {
        this.repository = repository;
        this.auditService = auditService;
    }

    public List<Map<String, Object>> findOrganizations() {
        return repository.findOrganizations();
    }

    public CpfPage<Map<String,Object>> findOrganizationsPage(Integer page,Integer size) {
        return repository.organizationPage(CpfPageRequest.of(page,size));
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> saveOrganization(OrganizationRequest request, String operatorId) {
        String code = required(request.organizationCode(), "organizationCode").toUpperCase(Locale.ROOT);
        String user = required(operatorId, "operatorId");
        String parent = blankToNull(request.parentOrganizationCode());
        if (parent != null) parent = parent.toUpperCase(Locale.ROOT);
        if (repository.wouldCreateOrganizationCycle(code, parent)) {
            throw new CpfValidationException("상위 조직 지정으로 조직 순환이 발생합니다. organizationCode=" + code);
        }
        boolean exists = repository.organizationExists(code);
        if (exists && request.expectedVersion() == null) {
            throw new CpfValidationException("조직 수정에는 expectedVersion이 필요합니다.");
        }
        Map<String,Object> values=new LinkedHashMap<>();
        values.put("organizationCode",code); values.put("parentOrganizationCode",parent);
        values.put("organizationName",required(request.organizationName(),"organizationName"));
        values.put("organizationType",defaultText(request.organizationType(),"DEPARTMENT").toUpperCase(Locale.ROOT));
        values.put("sortOrder",request.sortOrder()==null?0:request.sortOrder());
        values.put("effectiveFrom",request.effectiveFrom()); values.put("effectiveTo",request.effectiveTo());
        if (request.effectiveFrom()!=null && request.effectiveTo()!=null && !request.effectiveTo().isAfter(request.effectiveFrom())) {
            throw new CpfValidationException("effectiveTo는 effectiveFrom보다 뒤여야 합니다.");
        }
        values.put("useYn",yn(request.useYn(),"Y")); values.put("expectedVersion",request.expectedVersion()); values.put("requestUser",user);
        int changed=repository.saveOrganization(values);
        if(changed!=1) throw new CpfValidationException("조직이 다른 관리자에 의해 변경되었습니다. 다시 조회하십시오.");
        audit(user,"ORGANIZATION_SAVE","bza_organization",code,required(request.reason(),"reason"),null,values);
        return values;
    }

    public List<Map<String, Object>> findEmployees(String organizationCode, String status) {
        return repository.findEmployees(blankToNull(organizationCode), blankToNull(status));
    }

    public CpfPage<Map<String,Object>> findEmployeesPage(String organizationCode,String status,Integer page,Integer size) {
        return repository.employeePage(blankToNull(organizationCode),blankToNull(status),CpfPageRequest.of(page,size));
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> saveEmployee(EmployeeRequest request, String operatorId) {
        String employeeNo=required(request.employeeNo(),"employeeNo").toUpperCase(Locale.ROOT);
        String user=required(operatorId,"operatorId");
        if (request.leaveDate()!=null && request.joinDate()!=null && request.leaveDate().isBefore(request.joinDate())) {
            throw new CpfValidationException("leaveDate는 joinDate보다 빠를 수 없습니다.");
        }
        Map<String,Object> values=new LinkedHashMap<>();
        values.put("employeeNo",employeeNo); values.put("adminUserId",request.adminUserId());
        values.put("organizationCode",required(request.organizationCode(),"organizationCode").toUpperCase(Locale.ROOT));
        values.put("employeeName",required(request.employeeName(),"employeeName")); values.put("positionCode",blankToNull(request.positionCode()));
        values.put("jobTitleCode",blankToNull(request.jobTitleCode())); values.put("managerEmployeeNo",blankToNull(request.managerEmployeeNo()));
        values.put("employmentStatus",defaultText(request.employmentStatus(),"EMPLOYED").toUpperCase(Locale.ROOT));
        values.put("joinDate",request.joinDate()); values.put("leaveDate",request.leaveDate()); values.put("email",blankToNull(request.email()));
        values.put("mobileNo",blankToNull(request.mobileNo())); values.put("officePhoneNo",blankToNull(request.officePhoneNo()));
        values.put("useYn",yn(request.useYn(),"Y"));
        values.put("expectedVersion",request.expectedVersion()); values.put("requestUser",user);
        int changed=repository.saveEmployee(values);
        if(changed!=1) throw new CpfValidationException("직원 정보가 다른 관리자에 의해 변경되었거나 expectedVersion이 올바르지 않습니다.");
        audit(user,"EMPLOYEE_SAVE","bza_employee",employeeNo,required(request.reason(),"reason"),null,values);
        return values;
    }

    public List<Map<String, Object>> findEffectivePermissions(String loginId) {
        return repository.findEffectivePermissions(required(loginId, "loginId"));
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> createApproval(ApprovalCreateRequest request, String operatorId) {
        throw new CpfValidationException("Legacy 직접 결재 생성은 서비스 계층에서도 금지됩니다. 정책 기반 Approval Engine을 사용하십시오.");
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
        throw new CpfValidationException("Legacy 직접 결재 상태변경은 서비스 계층에서도 금지됩니다. 정책 기반 Approval Engine을 사용하십시오.");
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
            String actor, String action, String targetType, String targetId,
            String reason, Object before, Object after) {
        auditService.record(actor, action, targetType, targetId, reason, before, after);
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
        return CpfStrings.requireText(value, field);
    }

    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    /**
     * 기존 사람별 직접 결재선 API가 신규 정책 엔진의 의미를 과장하지 않도록
     * 현재 구현 가능한 ALL 규칙만 표준 값으로 정규화합니다.
     *
     * <p>ANY/N_OF_M, 조직/Role Target, 병렬 부서합의는 정책/참여자 Snapshot 엔진이
     * 구현된 뒤 별도 경로로 처리해야 합니다. 현재 direct-line 경로에서 값을 받아 놓고
     * ALL처럼 처리하면 잘못된 완료 판정이 되므로 fail-closed 합니다.</p>
     */
    private String normalizeLegacyDirectLineRule(String value) {
        String normalized = defaultText(value, "ALL").toUpperCase(Locale.ROOT);
        if ("ALL_APPROVE".equals(normalized)) {
            return "ALL";
        }
        if (!"ALL".equals(normalized)) {
            throw new CpfValidationException(
                    "기존 직접 결재선은 ALL만 지원합니다. ANY/N_OF_M/부서합의는 정책 기반 Approval Engine을 사용해야 합니다.");
        }
        return normalized;
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
            String organizationCode, String parentOrganizationCode, String organizationName,
            String organizationType, Integer sortOrder, LocalDateTime effectiveFrom, LocalDateTime effectiveTo,
            String useYn, Long expectedVersion, String requestUser, String reason) { }

    public record EmployeeRequest(
            String employeeNo, Long adminUserId, String organizationCode, String employeeName,
            String positionCode, String jobTitleCode, String managerEmployeeNo, String employmentStatus,
            LocalDate joinDate, LocalDate leaveDate, String email, String mobileNo, String officePhoneNo,
            String useYn, Long expectedVersion, String requestUser, String reason) {
        /** 기존 Consumer의 생성자 계약을 유지하면서 내부 전화번호를 선택 필드로 확장합니다. */
        public EmployeeRequest(
                String employeeNo, Long adminUserId, String organizationCode, String employeeName,
                String positionCode, String jobTitleCode, String managerEmployeeNo, String employmentStatus,
                LocalDate joinDate, LocalDate leaveDate, String email, String mobileNo,
                String useYn, Long expectedVersion, String requestUser, String reason) {
            this(employeeNo, adminUserId, organizationCode, employeeName, positionCode, jobTitleCode,
                    managerEmployeeNo, employmentStatus, joinDate, leaveDate, email, mobileNo, null,
                    useYn, expectedVersion, requestUser, reason);
        }
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
