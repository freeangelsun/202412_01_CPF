package cpf.bizadm.sample.controller;

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
 * BIZADM 교육/호환용 API입니다.
 *
 * <p>이 컨트롤러는 과거 샘플 경로를 유지하기 위한 얇은 래퍼입니다.
 * 응답 데이터는 하드코딩하지 않고 운영용 {@link BizAdmOperationService}를 통해 조회합니다.</p>
 */
@RestController
@RequestMapping("/api/bizadm/sample")
@Tag(name = "BIZADM-Sample", description = "BIZADM 교육/호환 API")
public class BizAdmSampleController {

    private final BizAdmOperationService operationService;

    public BizAdmSampleController(BizAdmOperationService operationService) {
        this.operationService = operationService;
    }

    @GetMapping("/admin-users")
    @CpfTransaction(id = "BIZ01ADM0001", name = "BizAdmSampleAdminUserList")
    @Operation(summary = "업무 관리자 사용자 조회 예시", description = "운영 저장소를 사용해 업무 관리자 사용자 목록을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findAdminUsers() {
        return ResponseEntity.ok(operationService.findAdminUsers());
    }

    @GetMapping("/menus")
    @CpfTransaction(id = "BIZ01MNU0001", name = "BizAdmSampleMenuList")
    @Operation(summary = "업무 관리자 메뉴 조회 예시", description = "운영 저장소를 사용해 업무 관리자 메뉴 목록을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findMenus() {
        return ResponseEntity.ok(operationService.findMenus());
    }

    @GetMapping("/roles")
    @CpfTransaction(id = "BIZ01ROL0001", name = "BizAdmSampleRoleList")
    @Operation(summary = "업무 관리자 역할 조회 예시", description = "운영 저장소를 사용해 업무 관리자 역할 목록을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findRoles() {
        return ResponseEntity.ok(operationService.findRoles());
    }

    @GetMapping("/permissions")
    @CpfTransaction(id = "BIZ01PER0001", name = "BizAdmSamplePermissionList")
    @Operation(summary = "업무 관리자 권한 조회 예시", description = "운영 저장소를 사용해 메뉴/버튼 권한 매트릭스를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findPermissions() {
        return ResponseEntity.ok(operationService.findPermissions());
    }

    @GetMapping("/customers")
    @CpfTransaction(id = "BIZ01CUS0001", name = "BizAdmSampleCustomerList")
    @Operation(summary = "고객 조회 예시", description = "운영 저장소를 사용해 마스킹된 고객 목록을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findCustomers() {
        return ResponseEntity.ok(operationService.findCustomers());
    }

    @PostMapping("/masking/unmask")
    @CpfTransaction(id = "BIZ02MSK0001", name = "BizAdmSampleUnmask")
    @Operation(summary = "마스킹 해제 예시", description = "감사 사유를 필수로 받아 고객 원문 조회 감사 이력을 남깁니다.")
    public ResponseEntity<List<Map<String, Object>>> unmaskCustomers(
            @RequestParam String reason,
            @RequestParam(defaultValue = "BIZADM_SAMPLE") String requestUser) {
        return ResponseEntity.ok(operationService.unmaskCustomers(reason, requestUser));
    }

    @GetMapping("/products")
    @CpfTransaction(id = "BIZ01PRD0001", name = "BizAdmSampleProductList")
    @Operation(summary = "상품 조회 예시", description = "운영 저장소를 사용해 상품 기준정보를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findProducts() {
        return ResponseEntity.ok(operationService.findProducts());
    }

    @GetMapping("/orders")
    @CpfTransaction(id = "BIZ01ORD0001", name = "BizAdmSampleOrderList")
    @Operation(summary = "주문 조회 예시", description = "운영 저장소를 사용해 주문 목록을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findOrders() {
        return ResponseEntity.ok(operationService.findOrders());
    }

    @GetMapping("/settings")
    @CpfTransaction(id = "BIZ01SET0001", name = "BizAdmSampleSettingList")
    @Operation(summary = "업무 설정 조회 예시", description = "운영 저장소를 사용해 업무 설정을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findSettings() {
        return ResponseEntity.ok(operationService.findSettings());
    }

    @GetMapping("/downloads")
    @CpfTransaction(id = "BIZ01DWN0001", name = "BizAdmSampleDownloadPolicyList")
    @Operation(summary = "다운로드 정책 조회 예시", description = "운영 저장소를 사용해 다운로드 정책을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findDownloadPolicies() {
        return ResponseEntity.ok(operationService.findDownloadPolicies());
    }
}
