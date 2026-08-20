package com.cpf.backoffice.online.management.controller;

import com.cpf.web.api.CpfRestController;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;

import com.cpf.backoffice.online.management.dto.BackofficeEmployeeRawContactResponse;
import com.cpf.backoffice.online.management.service.BackofficeManagementService;
import com.cpf.foundation.api.page.CpfPage;
import com.cpf.security.api.CpfSensitiveDataAccessRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/** MBW 범용 백오피스와 결재 capability API입니다. */
@CpfRestController
@RequestMapping("/api/v1/backoffice/backoffice")
@Tag(name = "MBW-Backoffice", description = "MBW 조직·직원·실효 권한·결재·업무 감사 API")
public class BackofficeManagementController extends com.cpf.backoffice.online.base.BackofficeBaseController {
    private final BackofficeManagementService backofficeService;

    public BackofficeManagementController(BackofficeManagementService backofficeService) {
        this.backofficeService = backofficeService;
    }

    @GetMapping("/organizations")    @Operation(operationId = "MBW_BACKOFFICE_FIND_ORGANIZATIONS", summary = "조직 목록 조회")
    @CpfOnlineTransaction(operationId = "MBW_BACKOFFICE_FIND_ORGANIZATIONS", name = "조직 목록 조회", description = "조직 목록 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<List<Map<String, Object>>> organizations() {
        return ResponseEntity.ok(backofficeService.findOrganizations());
    }

    @GetMapping("/organizations/page")    @Operation(operationId = "MBW_BACKOFFICE_FIND_ORGANIZATIONS_PAGE", summary = "조직 서버 Paging 조회")
    @CpfOnlineTransaction(operationId = "MBW_BACKOFFICE_FIND_ORGANIZATIONS_PAGE", name = "조직 서버 Paging 조회", description = "조직 서버 Paging 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<CpfPage<Map<String,Object>>> organizationsPage(
            @RequestParam(required=false) Integer page,@RequestParam(required=false) Integer size) {
        return ResponseEntity.ok(backofficeService.findOrganizationsPage(page,size));
    }

    @PostMapping("/organizations")    @Operation(operationId = "MBW_BACKOFFICE_SAVE_ORGANIZATION", summary = "조직 등록·수정",
            description = "조직 코드를 기준으로 등록하거나 수정하고 업무 감사를 기록합니다.")
    @CpfOnlineTransaction(operationId = "MBW_BACKOFFICE_SAVE_ORGANIZATION", name = "조직 등록·수정", description = "조직 등록·수정 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<Map<String, Object>> saveOrganization(
            @RequestBody BackofficeManagementService.OrganizationRequest request,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(backofficeService.saveOrganization(request, operatorId));
    }

    @GetMapping("/employees")    @Operation(operationId = "MBW_BACKOFFICE_FIND_EMPLOYEES", summary = "직원 목록 조회")
    @CpfOnlineTransaction(operationId = "MBW_BACKOFFICE_FIND_EMPLOYEES", name = "직원 목록 조회", description = "직원 목록 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<List<Map<String, Object>>> employees(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(backofficeService.findEmployees(organizationCode, status));
    }

    @GetMapping("/employees/page")    @Operation(operationId = "MBW_BACKOFFICE_FIND_EMPLOYEES_PAGE", summary = "직원 서버 Paging 조회")
    @CpfOnlineTransaction(operationId = "MBW_BACKOFFICE_FIND_EMPLOYEES_PAGE", name = "직원 서버 Paging 조회", description = "직원 서버 Paging 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<CpfPage<Map<String,Object>>> employeesPage(
            @RequestParam(required=false) String organizationCode,@RequestParam(required=false) String status,
            @RequestParam(required=false) Integer page,@RequestParam(required=false) Integer size) {
        return ResponseEntity.ok(backofficeService.findEmployeesPage(organizationCode,status,page,size));
    }

    @PostMapping("/employees/{employeeNo}/contacts/raw")    @Operation(operationId = "MBW_BACKOFFICE_EMPLOYEE_RAW_CONTACT", summary = "직원 연락처 원문 조회",
            description = "PII_RAW 권한과 사유가 있는 경우에만 원문 연락처를 반환하고 감사 기록을 남깁니다.")
    @CpfOnlineTransaction(operationId = "MBW_BACKOFFICE_EMPLOYEE_RAW_CONTACT", name = "직원 연락처 원문 조회", description = "직원 연락처 원문 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<BackofficeEmployeeRawContactResponse> employeeRawContact(
            @PathVariable String employeeNo,
            @RequestBody CpfSensitiveDataAccessRequest request,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(backofficeService.findEmployeeRaw(employeeNo,operatorId,request.reason()));
    }

    @PostMapping("/employees")    @Operation(operationId = "MBW_BACKOFFICE_SAVE_EMPLOYEE", summary = "직원 등록·수정",
            description = "직원 프로필과 대표 조직·직급·직책을 등록하거나 수정합니다. 부재·대행은 Assignment/조직 책임 정본 API를 사용합니다.")
    @CpfOnlineTransaction(operationId = "MBW_BACKOFFICE_SAVE_EMPLOYEE", name = "직원 등록·수정", description = "직원 등록·수정 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<Map<String, Object>> saveEmployee(
            @RequestBody BackofficeManagementService.EmployeeRequest request,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        return ResponseEntity.ok(backofficeService.saveEmployee(request, operatorId));
    }

    @GetMapping("/permissions/effective")    @Operation(operationId = "MBW_BACKOFFICE_FIND_EFFECTIVE_PERMISSIONS", summary = "사용자 실효 권한 조회",
            description = "역할에 연결된 화면·버튼·API와 데이터 범위 권한을 조회합니다.")
    @CpfOnlineTransaction(operationId = "MBW_BACKOFFICE_FIND_EFFECTIVE_PERMISSIONS", name = "사용자 실효 권한 조회", description = "사용자 실효 권한 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<List<Map<String, Object>>> effectivePermissions(@RequestParam String loginId) {
        return ResponseEntity.ok(backofficeService.findEffectivePermissions(loginId));
    }

    @GetMapping("/approvals")
    @Hidden    @Operation(operationId = "MBW_BACKOFFICE_FIND_APPROVALS", summary = "Legacy 결재 목록 API(410)",
            description = "직접 결재 API는 영구 폐기되었습니다. 정책 기반 /api/v1/backoffice/approvals/** API를 사용합니다.")
    public ResponseEntity<List<Map<String, Object>>> approvals(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(defaultValue = "100") int limit) {
        throw legacyApprovalGone();
    }

    @PostMapping("/approvals")
    @Hidden    @Operation(operationId = "MBW_BACKOFFICE_CREATE_APPROVAL", summary = "Legacy 결재 작성 API(410)", deprecated = true,
            description = "직접 결재 API는 영구 폐기되었습니다. 정책 기반 /api/v1/backoffice/approvals/** API를 사용합니다.")
    public ResponseEntity<Map<String, Object>> createApproval(
            @RequestBody(required = false) Map<String, Object> request,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        throw legacyApprovalGone();
    }

    @GetMapping("/approvals/{approvalId}")
    @Hidden    @Operation(operationId = "MBW_BACKOFFICE_FIND_APPROVAL", summary = "Legacy 결재 상세 API(410)",
            description = "직접 결재 API는 영구 폐기되었습니다. 정책 기반 /api/v1/backoffice/approvals/** API를 사용합니다.")
    public ResponseEntity<Map<String, Object>> approval(@PathVariable long approvalId) {
        throw legacyApprovalGone();
    }

    @PostMapping("/approvals/{approvalId}/actions")
    @Hidden    @Operation(operationId = "MBW_BACKOFFICE_ACT_APPROVAL", summary = "Legacy 결재 상태 변경 API(410)", deprecated = true,
            description = "직접 결재 API는 영구 폐기되었습니다. 정책 기반 /api/v1/backoffice/approvals/** API를 사용합니다.")
    public ResponseEntity<Map<String, Object>> actApproval(
            @PathVariable long approvalId,
            @RequestBody(required = false) Map<String, Object> request,
            @RequestAttribute("backoffice.operatorId") String operatorId) {
        throw legacyApprovalGone();
    }

    private ResponseStatusException legacyApprovalGone() {
        return new ResponseStatusException(
                HttpStatus.GONE,
                "Legacy 직접 결재 API는 폐기되었습니다. 정책 기반 /api/v1/backoffice/approvals/** API를 사용하세요.");
    }

    @GetMapping("/audits")    @Operation(operationId = "MBW_BACKOFFICE_FIND_BUSINESS_AUDITS", summary = "업무 감사 목록 조회")
    @CpfOnlineTransaction(operationId = "MBW_BACKOFFICE_FIND_BUSINESS_AUDITS", name = "업무 감사 목록 조회", description = "업무 감사 목록 조회 업무 거래를 CPF 표준 계약에 따라 처리한다.")
    public ResponseEntity<List<Map<String, Object>>> audits(@RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(backofficeService.findAudits(limit));
    }
}
