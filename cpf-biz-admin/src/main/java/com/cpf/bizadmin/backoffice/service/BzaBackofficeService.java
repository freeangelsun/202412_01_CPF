package com.cpf.bizadmin.backoffice.service;

import com.cpf.bizadmin.backoffice.dto.BzaEmployeeRawContactResponse;
import com.cpf.bizadmin.backoffice.repository.BzaBackofficeRepository;
import com.cpf.bizadmin.audit.service.BzaBusinessAuditService;
import com.cpf.bizadmin.common.model.BzaEmploymentStatus;
import com.cpf.core.api.util.CpfStrings;
import com.cpf.core.api.page.CpfPage;
import com.cpf.core.api.page.CpfPageRequest;
import com.cpf.core.api.error.CpfNotFoundException;
import com.cpf.core.api.error.CpfValidationException;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.security.CpfSensitiveData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** BZA 조직·직원·실효 권한·결재 상태 전이를 담당합니다. */
@Service
public class BzaBackofficeService extends com.cpf.bizadmin.common.base.BzaBaseService {
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
        return repository.findEmployees(blankToNull(organizationCode), normalizeEmploymentStatusFilter(status)).stream()
                .map(this::maskEmployee).toList();
    }

    public CpfPage<Map<String,Object>> findEmployeesPage(String organizationCode,String status,Integer page,Integer size) {
        CpfPage<Map<String,Object>> result = repository.employeePage(
                blankToNull(organizationCode), normalizeEmploymentStatusFilter(status), CpfPageRequest.of(page,size));
        return new CpfPage<>(result.content().stream().map(this::maskEmployee).toList(),
                result.totalElements(), result.page(), result.size());
    }

    @Transactional(transactionManager = "bzaTransactionManager", readOnly = false)
    public BzaEmployeeRawContactResponse findEmployeeRaw(String employeeNo, String operatorId, String reason) {
        String id = required(employeeNo, "employeeNo").toUpperCase(Locale.ROOT);
        String actor = required(operatorId, "operatorId");
        String safeReason = CpfSensitiveData.sanitizeAuditReason(reason);
        Map<String,Object> raw = repository.findEmployeeRawContact(id)
                .orElseThrow(() -> new CpfNotFoundException("직원을 찾을 수 없습니다. employeeNo=" + id));

        // 감사 기록이 실패하면 Transaction이 rollback되고 원문 응답 생성까지 도달하지 않습니다.
        audit(actor, "EMPLOYEE_PII_RAW_VIEW", "bza_employee", id, safeReason, null, Map.of("rawView", true));
        return new BzaEmployeeRawContactResponse(
                id, stringValue(raw.get("email")), stringValue(raw.get("mobileNo")),
                stringValue(raw.get("officePhoneNo")), true, CpfTransactionContext.transactionId());
    }

    @Transactional(transactionManager = "bzaTransactionManager")
    public Map<String, Object> saveEmployee(EmployeeRequest request, String operatorId) {
        String employeeNo=required(request.employeeNo(),"employeeNo").toUpperCase(Locale.ROOT);
        String user=required(operatorId,"operatorId");
        if (request.leaveDate()!=null && request.joinDate()!=null && request.leaveDate().isBefore(request.joinDate())) {
            throw new CpfValidationException("leaveDate는 joinDate보다 빠를 수 없습니다.");
        }
        Map<String,Object> before=repository.findEmployee(employeeNo).orElse(null);
        if (before!=null && request.expectedVersion()==null) {
            throw new CpfValidationException("직원 수정에는 expectedVersion이 필요합니다.");
        }
        String status=BzaEmploymentStatus.parse(defaultText(request.employmentStatus(),"EMPLOYED")).name();
        Map<String,Object> values=new LinkedHashMap<>();
        values.put("employeeNo",employeeNo); values.put("adminUserId",request.adminUserId());
        values.put("organizationCode",required(request.organizationCode(),"organizationCode").toUpperCase(Locale.ROOT));
        values.put("employeeName",required(request.employeeName(),"employeeName"));
        values.put("positionCode",blankToNull(request.positionCode()));
        values.put("jobTitleCode",blankToNull(request.jobTitleCode()));
        values.put("managerEmployeeNo",blankToNull(request.managerEmployeeNo()));
        values.put("employmentStatus",status);
        values.put("joinDate",request.joinDate()); values.put("leaveDate",request.leaveDate());

        String existingEmail=before==null?null:stringValue(before.get("email"));
        String existingMobile=before==null?null:stringValue(before.get("mobileNo"));
        String existingOffice=before==null?null:stringValue(before.get("officePhoneNo"));
        values.put("email",resolveContact(request.email(),request.clearEmail(),existingEmail,true,"email"));
        values.put("mobileNo",resolveContact(request.mobileNo(),request.clearMobileNo(),existingMobile,false,"mobileNo"));
        values.put("officePhoneNo",resolveContact(request.officePhoneNo(),request.clearOfficePhoneNo(),existingOffice,false,"officePhoneNo"));
        values.put("useYn",yn(request.useYn(),"Y"));
        values.put("expectedVersion",request.expectedVersion()); values.put("requestUser",user);

        int changed=repository.saveEmployee(values);
        if(changed!=1) throw new CpfValidationException("직원 정보가 다른 관리자에 의해 변경되었거나 expectedVersion이 올바르지 않습니다.");
        Map<String,Object> after=repository.findEmployee(employeeNo).orElse(values);
        audit(user,"EMPLOYEE_SAVE","bza_employee",employeeNo,required(request.reason(),"reason"),before,after);
        return maskEmployee(after);
    }

    public List<Map<String, Object>> findEffectivePermissions(String loginId) {
        return repository.findEffectivePermissions(required(loginId, "loginId"));
    }

    public List<Map<String, Object>> findAudits(int limit) {
        return repository.findBusinessAudits(Math.max(1, Math.min(limit, 500)));
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

    private String normalizeEmploymentStatusFilter(String value) {
        String normalized = blankToNull(value);
        if (normalized == null) return null;
        return BzaEmploymentStatus.parse(normalized).name();
    }

    private Map<String,Object> maskEmployee(Map<String,Object> row) {
        Map<String,Object> masked=new LinkedHashMap<>(row);
        masked.put("email", CpfSensitiveData.maskEmail(stringValue(row.get("email"))));
        masked.put("mobileNo", CpfSensitiveData.maskPhone(stringValue(row.get("mobileNo"))));
        masked.put("officePhoneNo", CpfSensitiveData.maskPhone(stringValue(row.get("officePhoneNo"))));
        masked.put("rawViewAllowed", false);
        return masked;
    }

    private String resolveContact(String requested, boolean clear, String existing, boolean email, String field) {
        if(clear) return null;
        if(requested==null || requested.isBlank()) return existing;
        return email ? CpfSensitiveData.normalizeEmail(requested,field)
                : CpfSensitiveData.normalizePhone(requested,field);
    }

    private String stringValue(Object value) {
        return value==null?null:String.valueOf(value);
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

    public record OrganizationRequest(
            String organizationCode, String parentOrganizationCode, String organizationName,
            String organizationType, Integer sortOrder, LocalDateTime effectiveFrom, LocalDateTime effectiveTo,
            String useYn, Long expectedVersion, String requestUser, String reason) { }

    public record EmployeeRequest(
            String employeeNo, Long adminUserId, String organizationCode, String employeeName,
            String positionCode, String jobTitleCode, String managerEmployeeNo, String employmentStatus,
            LocalDate joinDate, LocalDate leaveDate, String email, String mobileNo, String officePhoneNo,
            boolean clearEmail, boolean clearMobileNo, boolean clearOfficePhoneNo,
            String useYn, Long expectedVersion, String requestUser, String reason) {
        public EmployeeRequest(
                String employeeNo, Long adminUserId, String organizationCode, String employeeName,
                String positionCode, String jobTitleCode, String managerEmployeeNo, String employmentStatus,
                LocalDate joinDate, LocalDate leaveDate, String email, String mobileNo, String officePhoneNo,
                String useYn, Long expectedVersion, String requestUser, String reason) {
            this(employeeNo, adminUserId, organizationCode, employeeName, positionCode, jobTitleCode,
                    managerEmployeeNo, employmentStatus, joinDate, leaveDate, email, mobileNo, officePhoneNo,
                    false, false, false, useYn, expectedVersion, requestUser, reason);
        }

        /** 기존 Consumer의 생성자 계약을 유지하면서 내부 전화번호를 선택 필드로 확장합니다. */
        public EmployeeRequest(
                String employeeNo, Long adminUserId, String organizationCode, String employeeName,
                String positionCode, String jobTitleCode, String managerEmployeeNo, String employmentStatus,
                LocalDate joinDate, LocalDate leaveDate, String email, String mobileNo,
                String useYn, Long expectedVersion, String requestUser, String reason) {
            this(employeeNo, adminUserId, organizationCode, employeeName, positionCode, jobTitleCode,
                    managerEmployeeNo, employmentStatus, joinDate, leaveDate, email, mobileNo, null,
                    false, false, false, useYn, expectedVersion, requestUser, reason);
        }
    }


}
