package com.cpf.bizadmin.operation.controller;

import com.cpf.bizadmin.operation.service.BzaOperationService;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * BZA 업무 관리자 운영 API입니다.
 *
 * <p>샘플 하드코딩 응답이 아니라 bzaDB 기준 운영 데이터를 반환합니다.
 * 권한, 마스킹, 다운로드 정책 같은 업무 관리자 기능을 ADM 화면에서 연결할 수 있도록 제공합니다.</p>
 */
@RestController
@RequestMapping("/api/bza")
@Tag(name = "BZA-Operations", description = "업무 관리자 사용자, 메뉴, 권한, 고객, 상품, 주문, 설정 운영 API")
public class BzaOperationController extends com.cpf.bizadmin.common.base.BzaBaseController {
    private final BzaOperationService operationService;

    public BzaOperationController(BzaOperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping("/admin-users")
    @CpfOnlineTransaction(id = "OBZAAD1001", name = "BzaAdminUserList")
    @Operation(operationId = "bzaOperationFindAdminUsers", summary = "업무 관리자 사용자 조회", description = "bza_admin_user 기준 업무 관리자 사용자를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findAdminUsers() {
        return ResponseEntity.ok(operationService.findAdminUsers());
    }

    @PostMapping("/admin-users")
    @CpfOnlineTransaction(id = "OBZAAD1002", name = "BzaAdminUserSave")
    @Operation(operationId = "bzaOperationSaveAdminUser", summary = "업무 관리자 사용자 등록·수정",
            description = "사용자 계정과 역할·잠금·강제 비밀번호 변경 상태를 저장하고 변경 감사를 남깁니다.")
    public ResponseEntity<Map<String, Object>> saveAdminUser(
            @RequestBody BzaOperationService.AdminUserRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(operationService.saveAdminUser(request, operatorId));
    }

    @GetMapping("/menus")
    @CpfOnlineTransaction(id = "OBZAMN1001", name = "BzaMenuList")
    @Operation(operationId = "bzaOperationFindMenus", summary = "업무 관리자 메뉴 조회", description = "업무 관리자 메뉴와 모듈 구분을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findMenus() {
        return ResponseEntity.ok(operationService.findMenus());
    }

    @PostMapping("/menus")
    @CpfOnlineTransaction(id = "OBZAMN1002", name = "BzaMenuSave")
    @Operation(operationId = "bzaOperationSaveMenu", summary = "업무 관리자 메뉴 등록·수정",
            description = "메뉴 트리, 화면 route, 아이콘, API 경로와 적용 환경을 저장하고 변경 감사를 남깁니다.")
    public ResponseEntity<Map<String, Object>> saveMenu(
            @RequestBody BzaOperationService.MenuRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(operationService.saveMenu(request, operatorId));
    }

    @GetMapping("/roles")
    @CpfOnlineTransaction(id = "OBZARO1001", name = "BzaRoleList")
    @Operation(operationId = "bzaOperationFindRoles", summary = "업무 관리자 역할 조회", description = "업무 관리자 역할과 쓰기 허용 여부를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findRoles() {
        return ResponseEntity.ok(operationService.findRoles());
    }

    @PostMapping("/roles")
    @CpfOnlineTransaction(id = "OBZARO1002", name = "BzaRoleSave")
    @Operation(operationId = "bzaOperationSaveRole", summary = "업무 관리자 역할 등록·수정",
            description = "역할의 쓰기 허용 여부와 기본 데이터 범위를 저장하고 변경 감사를 남깁니다.")
    public ResponseEntity<Map<String, Object>> saveRole(
            @RequestBody BzaOperationService.RoleRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(operationService.saveRole(request, operatorId));
    }

    @GetMapping("/permissions")
    @CpfOnlineTransaction(id = "OBZAPE1001", name = "BzaPermissionList")
    @Operation(operationId = "bzaOperationFindPermissions", summary = "업무 관리자 권한 조회", description = "역할, 메뉴, 버튼 기준 업무 권한 매트릭스를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findPermissions() {
        return ResponseEntity.ok(operationService.findPermissions());
    }

    @PostMapping("/permissions")
    @CpfOnlineTransaction(id = "OBZAPE1002", name = "BzaPermissionSave")
    @Operation(operationId = "bzaOperationSavePermission", summary = "업무 관리자 권한 등록·수정",
            description = "역할별 화면·버튼·API 권한과 환경·업무·데이터 범위를 저장하고 변경 감사를 남깁니다.")
    public ResponseEntity<Map<String, Object>> savePermission(
            @RequestBody BzaOperationService.PermissionRequest request,
            @RequestAttribute("bza.operatorId") String operatorId) {
        return ResponseEntity.ok(operationService.savePermission(request, operatorId));
    }

    @GetMapping("/customers")
    @CpfOnlineTransaction(id = "OBZACU1001", name = "BzaCustomerList")
    @Operation(operationId = "bzaOperationFindCustomers", summary = "고객 조회", description = "고객 목록을 마스킹 기준으로 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findCustomers() {
        return ResponseEntity.ok(operationService.findCustomers());
    }

    @PostMapping("/masking/unmask")
    @CpfOnlineTransaction(id = "OBZAMS1001", name = "BzaUnmask")
    @Operation(operationId = "bzaOperationUnmaskCustomers", summary = "고객 마스킹 해제", description = "감사 사유를 필수로 받아 고객 원문 정보를 조회하고 마스킹 감사 이력을 남깁니다.")
    public ResponseEntity<List<Map<String, Object>>> unmaskCustomers(
            @RequestParam String reason,
            @RequestParam(defaultValue = "BZA_OPERATOR") String requestUser) {
        return ResponseEntity.ok(operationService.unmaskCustomers(reason, requestUser));
    }

    @GetMapping("/products")
    @CpfOnlineTransaction(id = "OBZAPR1001", name = "BzaProductList")
    @Operation(operationId = "bzaOperationFindProducts", summary = "상품 조회", description = "업무 상품 기준정보를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findProducts() {
        return ResponseEntity.ok(operationService.findProducts());
    }

    @GetMapping("/orders")
    @CpfOnlineTransaction(id = "OBZAOR1001", name = "BzaOrderList")
    @Operation(operationId = "bzaOperationFindOrders", summary = "주문 조회", description = "업무 주문과 고객/상품 연결 정보를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findOrders() {
        return ResponseEntity.ok(operationService.findOrders());
    }

    @GetMapping("/settings")
    @CpfOnlineTransaction(id = "OBZASE1001", name = "BzaSettingList")
    @Operation(operationId = "bzaOperationFindSettings", summary = "업무 설정 조회", description = "업무 모듈 적용 설정을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findSettings() {
        return ResponseEntity.ok(operationService.findSettings());
    }

    @GetMapping("/downloads")
    @CpfOnlineTransaction(id = "OBZADW1001", name = "BzaDownloadPolicyList")
    @Operation(operationId = "bzaOperationFindDownloadPolicies", summary = "다운로드 정책 조회", description = "업무 다운로드 정책 설정을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findDownloadPolicies() {
        return ResponseEntity.ok(operationService.findDownloadPolicies());
    }
}
