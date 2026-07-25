package com.cpf.bizadmin.directory.service;

import com.cpf.bizadmin.directory.repository.BzaDirectoryRepository;
import com.cpf.core.common.exception.CpfValidationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/** BZA 조직/직급/직책/발령/조직책임/다중 Role 관리 서비스. */
@Service
public class BzaDirectoryService {
    private static final java.util.Set<String> ASSIGNMENT_TYPES =
            java.util.Set.of("PRIMARY", "CONCURRENT", "SECONDMENT", "ACTING");
    private static final java.util.Set<String> RESPONSIBILITY_TYPES =
            java.util.Set.of("MANAGER", "DEPUTY", "ACTING", "APPROVAL_OWNER");

    private final BzaDirectoryRepository repository;

    public BzaDirectoryService(BzaDirectoryRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> findPositions() { return repository.findPositions(); }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> savePosition(PositionRequest request, String operatorId) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("positionCode", required(request.positionCode(), "positionCode"));
        values.put("positionName", required(request.positionName(), "positionName"));
        values.put("rankOrder", request.rankOrder() == null ? 0 : request.rankOrder());
        values.put("useYn", yn(request.useYn(), "Y"));
        values.put("operatorId", required(operatorId, "operatorId"));
        repository.savePosition(values);
        return Map.of("saved", true, "positionCode", values.get("positionCode"));
    }

    public List<Map<String, Object>> findJobTitles() { return repository.findJobTitles(); }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> saveJobTitle(JobTitleRequest request, String operatorId) {
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("jobTitleCode", required(request.jobTitleCode(), "jobTitleCode"));
        values.put("jobTitleName", required(request.jobTitleName(), "jobTitleName"));
        values.put("managerYn", yn(request.managerYn(), "N"));
        values.put("useYn", yn(request.useYn(), "Y"));
        values.put("operatorId", required(operatorId, "operatorId"));
        repository.saveJobTitle(values);
        return Map.of("saved", true, "jobTitleCode", values.get("jobTitleCode"));
    }

    public List<Map<String, Object>> findAssignments(String employeeNo, String organizationCode, Instant effectiveAt) {
        return repository.findAssignments(employeeNo, organizationCode, effectiveAt == null ? Instant.now() : effectiveAt);
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> saveAssignment(AssignmentRequest request, String operatorId) {
        String employeeNo = required(request.employeeNo(), "employeeNo");
        Instant from = required(request.effectiveFrom(), "effectiveFrom");
        Instant to = request.effectiveTo();
        if (to != null && !to.isAfter(from)) throw new CpfValidationException("effectiveTo는 effectiveFrom보다 뒤여야 합니다.");
        String assignmentType = normalized(request.assignmentType(), "PRIMARY");
        if (!ASSIGNMENT_TYPES.contains(assignmentType)) throw new CpfValidationException("지원하지 않는 assignmentType입니다.");
        String primaryYn = yn(request.primaryYn(), "Y");
        if ("Y".equals(primaryYn)
                && repository.countOverlappingPrimaryAssignment(employeeNo, from, to, request.assignmentId()) > 0) {
            throw new CpfValidationException("같은 직원의 대표 소속 유효기간이 중복됩니다.");
        }
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("assignmentId", request.assignmentId());
        values.put("employeeNo", employeeNo);
        values.put("organizationCode", required(request.organizationCode(), "organizationCode"));
        values.put("positionCode", emptyToNull(request.positionCode()));
        values.put("jobTitleCode", emptyToNull(request.jobTitleCode()));
        values.put("assignmentType", assignmentType);
        values.put("primaryYn", primaryYn);
        values.put("effectiveFrom", Timestamp.from(from));
        values.put("effectiveTo", to == null ? null : Timestamp.from(to));
        values.put("operatorId", required(operatorId, "operatorId"));
        repository.saveAssignment(values);
        return Map.of("saved", true, "employeeNo", employeeNo);
    }

    public List<Map<String, Object>> findResponsibilities(String organizationCode, Instant effectiveAt) {
        return repository.findResponsibilities(organizationCode, effectiveAt == null ? Instant.now() : effectiveAt);
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> saveResponsibility(ResponsibilityRequest request, String operatorId) {
        Instant from = required(request.effectiveFrom(), "effectiveFrom");
        Instant to = request.effectiveTo();
        if (to != null && !to.isAfter(from)) throw new CpfValidationException("effectiveTo는 effectiveFrom보다 뒤여야 합니다.");
        String type = normalized(request.responsibilityType(), "MANAGER");
        if (!RESPONSIBILITY_TYPES.contains(type)) throw new CpfValidationException("지원하지 않는 responsibilityType입니다.");
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("responsibilityId", request.responsibilityId());
        values.put("organizationCode", required(request.organizationCode(), "organizationCode"));
        values.put("responsibilityType", type);
        values.put("employeeNo", required(request.employeeNo(), "employeeNo"));
        values.put("effectiveFrom", Timestamp.from(from));
        values.put("effectiveTo", to == null ? null : Timestamp.from(to));
        values.put("operatorId", required(operatorId, "operatorId"));
        repository.saveResponsibility(values);
        return Map.of("saved", true, "organizationCode", values.get("organizationCode"));
    }

    public List<Map<String, Object>> findUserRoles(String loginId, Instant effectiveAt) {
        return repository.findUserRoles(loginId, effectiveAt == null ? Instant.now() : effectiveAt);
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> saveUserRole(UserRoleRequest request, String operatorId) {
        Instant from = request.validFrom();
        Instant to = request.validTo();
        if (from != null && to != null && !to.isAfter(from)) throw new CpfValidationException("validTo는 validFrom보다 뒤여야 합니다.");
        Map<String, Object> values = new LinkedHashMap<>();
        values.put("loginId", required(request.loginId(), "loginId"));
        values.put("roleCode", required(request.roleCode(), "roleCode"));
        values.put("validFrom", from == null ? null : Timestamp.from(from));
        values.put("validTo", to == null ? null : Timestamp.from(to));
        values.put("primaryYn", yn(request.primaryYn(), "N"));
        values.put("operatorId", required(operatorId, "operatorId"));
        int changed = repository.saveUserRole(values);
        if (changed == 0) throw new CpfValidationException("존재하는 BZA 사용자를 찾을 수 없습니다.");
        return Map.of("saved", true, "loginId", values.get("loginId"), "roleCode", values.get("roleCode"));
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new CpfValidationException(field + "는 필수입니다.");
        return value.trim();
    }
    private static <T> T required(T value, String field) {
        if (value == null) throw new CpfValidationException(field + "는 필수입니다.");
        return value;
    }
    private static String yn(String value, String defaultValue) {
        String v = normalized(value, defaultValue);
        if (!v.equals("Y") && !v.equals("N")) throw new CpfValidationException("Y/N 값이 필요합니다.");
        return v;
    }
    private static String normalized(String value, String defaultValue) {
        return (value == null || value.isBlank() ? defaultValue : value.trim()).toUpperCase(Locale.ROOT);
    }
    private static String emptyToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }

    public record PositionRequest(String positionCode, String positionName, Integer rankOrder, String useYn, String reason) {}
    public record JobTitleRequest(String jobTitleCode, String jobTitleName, String managerYn, String useYn, String reason) {}
    public record AssignmentRequest(Long assignmentId, String employeeNo, String organizationCode, String positionCode,
                                    String jobTitleCode, String assignmentType, String primaryYn,
                                    Instant effectiveFrom, Instant effectiveTo, String reason) {}
    public record ResponsibilityRequest(Long responsibilityId, String organizationCode, String responsibilityType,
                                        String employeeNo, Instant effectiveFrom, Instant effectiveTo, String reason) {}
    public record UserRoleRequest(String loginId, String roleCode, Instant validFrom, Instant validTo,
                                  String primaryYn, String reason) {}
}
