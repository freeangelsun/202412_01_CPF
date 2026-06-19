package cpf.bizadm.operation.controller;

import cpf.bizadm.operation.service.BizAdmOperationService;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * BIZADM 업무 관리자 운영 API입니다.
 *
 * <p>샘플 하드코딩 응답이 아니라 bizadmDB 기준 운영 데이터를 반환합니다.
 * 권한, 마스킹, 다운로드 정책 같은 업무 관리자 기능을 ADM 화면에서 연결할 수 있도록 제공합니다.</p>
 */
@RestController
@RequestMapping("/api/bizadm")
@Tag(name = "BIZADM-Operations", description = "업무 관리자 사용자, 메뉴, 권한, 고객, 상품, 주문, 설정 운영 API")
public class BizAdmOperationController {
    private final BizAdmOperationService operationService;

    public BizAdmOperationController(BizAdmOperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping("/admin-users")
    @CpfTransaction(id = "BIZ01ADM1001", name = "BizAdmAdminUserList")
    @Operation(summary = "업무 관리자 사용자 조회", description = "bizadm_admin_user 기준 업무 관리자 사용자를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findAdminUsers() {
        return ResponseEntity.ok(operationService.findAdminUsers());
    }

    @GetMapping("/menus")
    @CpfTransaction(id = "BIZ01MNU1001", name = "BizAdmMenuList")
    @Operation(summary = "업무 관리자 메뉴 조회", description = "업무 관리자 메뉴와 모듈 구분을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findMenus() {
        return ResponseEntity.ok(operationService.findMenus());
    }

    @GetMapping("/roles")
    @CpfTransaction(id = "BIZ01ROL1001", name = "BizAdmRoleList")
    @Operation(summary = "업무 관리자 역할 조회", description = "업무 관리자 역할과 쓰기 허용 여부를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findRoles() {
        return ResponseEntity.ok(operationService.findRoles());
    }

    @GetMapping("/permissions")
    @CpfTransaction(id = "BIZ01PER1001", name = "BizAdmPermissionList")
    @Operation(summary = "업무 관리자 권한 조회", description = "역할, 메뉴, 버튼 기준 업무 권한 매트릭스를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findPermissions() {
        return ResponseEntity.ok(operationService.findPermissions());
    }

    @GetMapping("/customers")
    @CpfTransaction(id = "BIZ01CUS1001", name = "BizAdmCustomerList")
    @Operation(summary = "고객 조회", description = "고객 목록을 마스킹 기준으로 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findCustomers() {
        return ResponseEntity.ok(operationService.findCustomers());
    }

    @PostMapping("/masking/unmask")
    @CpfTransaction(id = "BIZ02MSK1001", name = "BizAdmUnmask")
    @Operation(summary = "고객 마스킹 해제", description = "감사 사유를 필수로 받아 고객 원문 정보를 조회하고 마스킹 감사 이력을 남깁니다.")
    public ResponseEntity<List<Map<String, Object>>> unmaskCustomers(
            @RequestParam String reason,
            @RequestParam(defaultValue = "BIZADM_OPERATOR") String requestUser) {
        return ResponseEntity.ok(operationService.unmaskCustomers(reason, requestUser));
    }

    @GetMapping("/products")
    @CpfTransaction(id = "BIZ01PRD1001", name = "BizAdmProductList")
    @Operation(summary = "상품 조회", description = "업무 상품 기준정보를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findProducts() {
        return ResponseEntity.ok(operationService.findProducts());
    }

    @GetMapping("/orders")
    @CpfTransaction(id = "BIZ01ORD1001", name = "BizAdmOrderList")
    @Operation(summary = "주문 조회", description = "업무 주문과 고객/상품 연결 정보를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findOrders() {
        return ResponseEntity.ok(operationService.findOrders());
    }

    @GetMapping("/settings")
    @CpfTransaction(id = "BIZ01SET1001", name = "BizAdmSettingList")
    @Operation(summary = "업무 설정 조회", description = "업무 모듈 적용 설정을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findSettings() {
        return ResponseEntity.ok(operationService.findSettings());
    }

    @GetMapping("/downloads")
    @CpfTransaction(id = "BIZ01DWN1001", name = "BizAdmDownloadPolicyList")
    @Operation(summary = "다운로드 정책 조회", description = "업무 다운로드 정책 설정을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findDownloadPolicies() {
        return ResponseEntity.ok(operationService.findDownloadPolicies());
    }
}
