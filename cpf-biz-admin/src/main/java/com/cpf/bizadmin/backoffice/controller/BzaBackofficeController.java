package com.cpf.bizadmin.backoffice.controller;

import com.cpf.bizadmin.backoffice.dto.BzaEmployeeRawContactResponse;
import com.cpf.bizadmin.backoffice.service.BzaBackofficeService;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.page.CpfPage;
import com.cpf.core.api.security.CpfSensitiveDataAccessRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/** BZA 범용 백오피스와 결재 capability API입니다. */
@RestController
@RequestMapping("/api/bza/backoffice")
@Tag(name = "BZA-Backoffice", description = "BZA 조직·직원·실효 권한·결재·업무 감사 API")
public class BzaBackofficeController extends com.cpf.bizadmin.common.base.BzaBaseController {
    private final BzaBackofficeService backofficeService;

    public BzaBackofficeController(BzaBackofficeService backofficeService) {
        this.backofficeService = backofficeService;
    }

    @GetMapping("/organizations")
    @CpfOnlineTransaction(id = "OBZAOR0001", name = "BzaOrganizationList")
    @Operation(operationId = "bzaBackofficeFindOrganizations", summary = "조직 목록 조회")
    public ResponseEntity<List<Map<String, Object>>> organizations() {
        return ResponseEntity.ok(backofficeService.findOrganizations());
    }

    @GetMapping("/organizations/page")
    @CpfOnlineTransaction(id = "OBZAOR1101", name = "BzaOrganizationPage")
    public ResponseEntity<CpfPage<Map<String,Object>>> organizationsPage(
            @RequestParam(required=false) Integer page,@RequestParam(required=false) Integer size) {
        return ResponseEntity.ok(backofficeService.findOrganizationsPage(page,size));
    }

    @PostMapping("/organizations")
    @CpfOnlineTransaction(id = "OBZAOR0002", name = "BzaOrganizationSave")
    @Operation(operationId = "bzaBackofficeSaveOrganization", summary = "조직 등록·수정",
            description = "조직 코드를 기준으로 등록하거나 수정하고 업무 감사를 기록합니다.")
    public ResponseEntity<Map<String, Object>> saveOrganization(
            @RequestBody BzaBackofficeService.OrganizationRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(backofficeService.saveOrganization(request, operatorId));
    }

    @GetMapping("/employees")
    @CpfOnlineTransaction(id = "OBZAEM0001", name = "BzaEmployeeList")
    @Operation(operationId = "bzaBackofficeFindEmployees", summary = "직원 목록 조회")
    public ResponseEntity<List<Map<String, Object>>> employees(
            @RequestParam(required = false) String organizationCode,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(backofficeService.findEmployees(organizationCode, status));
    }

    @GetMapping("/employees/page")
    @CpfOnlineTransaction(id = "OBZAEM1101", name = "BzaEmployeePage")
    public ResponseEntity<CpfPage<Map<String,Object>>> employeesPage(
            @RequestParam(required=false) String organizationCode,@RequestParam(required=false) String status,
            @RequestParam(required=false) Integer page,@RequestParam(required=false) Integer size) {
        return ResponseEntity.ok(backofficeService.findEmployeesPage(organizationCode,status,page,size));
    }

    @PostMapping("/employees/{employeeNo}/contacts/raw")
    @CpfOnlineTransaction(id = "OBZAEM1102", name = "BzaEmployeeRawContact")
    @Operation(operationId = "bzaBackofficeEmployeeRawContact", summary = "직원 연락처 원문 조회",
            description = "PII_RAW 권한과 사유가 있는 경우에만 원문 연락처를 반환하고 감사 기록을 남깁니다.")
    public ResponseEntity<BzaEmployeeRawContactResponse> employeeRawContact(
            @PathVariable String employeeNo,
            @RequestBody CpfSensitiveDataAccessRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(backofficeService.findEmployeeRaw(employeeNo,operatorId,request.reason()));
    }

    @PostMapping("/employees")
    @CpfOnlineTransaction(id = "OBZAEM0002", name = "BzaEmployeeSave")
    @Operation(operationId = "bzaBackofficeSaveEmployee", summary = "직원 등록·수정",
            description = "직원 프로필과 대표 조직·직급·직책을 등록하거나 수정합니다. 부재·대행은 Assignment/조직 책임 정본 API를 사용합니다.")
    public ResponseEntity<Map<String, Object>> saveEmployee(
            @RequestBody BzaBackofficeService.EmployeeRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(backofficeService.saveEmployee(request, operatorId));
    }

    @GetMapping("/permissions/effective")
    @CpfOnlineTransaction(id = "OBZAPE0002", name = "BzaEffectivePermissionList")
    @Operation(operationId = "bzaBackofficeFindEffectivePermissions", summary = "사용자 실효 권한 조회",
            description = "역할에 연결된 화면·버튼·API와 데이터 범위 권한을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> effectivePermissions(@RequestParam String loginId) {
        return ResponseEntity.ok(backofficeService.findEffectivePermissions(loginId));
    }

    @GetMapping("/approvals")
    @CpfOnlineTransaction(id = "OBZAAP0001", name = "BzaApprovalList")
    @Operation(operationId = "bzaBackofficeFindApprovals", summary = "Legacy 결재 목록 API(410)",
            description = "직접 결재 API는 영구 폐기되었습니다. 정책 기반 /api/bza/approval/** API를 사용합니다.")
    public ResponseEntity<List<Map<String, Object>>> approvals(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String employeeNo,
            @RequestParam(defaultValue = "100") int limit) {
        throw legacyApprovalGone();
    }

    @PostMapping("/approvals")
    @CpfOnlineTransaction(id = "OBZAAP0002", name = "BzaApprovalCreate")
    @Operation(operationId = "bzaBackofficeCreateApproval", summary = "결재 문서 작성",
            description = "업무 중립 결재 문서와 순차·병렬 결재선을 DRAFT 상태로 생성합니다.")
    public ResponseEntity<Map<String, Object>> createApproval(
            @RequestBody(required = false) Map<String, Object> request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        throw legacyApprovalGone();
    }

    @GetMapping("/approvals/{approvalId}")
    @CpfOnlineTransaction(id = "OBZAAP0003", name = "BzaApprovalDetail")
    @Operation(operationId = "bzaBackofficeFindApproval", summary = "Legacy 결재 상세 API(410)",
            description = "직접 결재 API는 영구 폐기되었습니다. 정책 기반 /api/bza/approval/** API를 사용합니다.")
    public ResponseEntity<Map<String, Object>> approval(@PathVariable long approvalId) {
        throw legacyApprovalGone();
    }

    @PostMapping("/approvals/{approvalId}/actions")
    @CpfOnlineTransaction(id = "OBZAAP0004", name = "BzaApprovalAction")
    @Operation(operationId = "bzaBackofficeActApproval", summary = "결재 상태 변경",
            description = "제출·승인·합의·반려·회수·취소·재제출을 상태표, 낙관적 잠금, 중복 방지 키로 보호합니다.")
    public ResponseEntity<Map<String, Object>> actApproval(
            @PathVariable long approvalId,
            @RequestBody(required = false) Map<String, Object> request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        throw legacyApprovalGone();
    }

    private ResponseStatusException legacyApprovalGone() {
        return new ResponseStatusException(
                HttpStatus.GONE,
                "Legacy 직접 결재 API는 폐기되었습니다. 정책 기반 /api/bza/approval/** API를 사용하세요.");
    }

    @GetMapping("/audits")
    @CpfOnlineTransaction(id = "OBZAUD0001", name = "BzaBusinessAuditList")
    @Operation(operationId = "bzaBackofficeFindBusinessAudits", summary = "업무 감사 목록 조회")
    public ResponseEntity<List<Map<String, Object>>> audits(@RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(backofficeService.findAudits(limit));
    }
}
