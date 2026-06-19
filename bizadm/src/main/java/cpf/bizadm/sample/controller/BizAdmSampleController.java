package cpf.bizadm.sample.controller;

import cpf.bizadm.sample.service.BizAdmSampleService;
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
 * 업무 관리자 화면에서 자주 필요한 메뉴, 권한, 마스킹, 다운로드 흐름을 보여주는 샘플 API입니다.
 */
@RestController
@RequestMapping("/api/bizadm")
@Tag(name = "BIZADM-Sample", description = "업무/프로젝트 관리자 샘플 API")
public class BizAdmSampleController {

    private final BizAdmSampleService sampleService;

    public BizAdmSampleController(BizAdmSampleService sampleService) {
        this.sampleService = sampleService;
    }

    @GetMapping("/admin-users")
    @CpfTransaction(id = "BIZ01ADM0001", name = "BizAdmAdminUserList")
    @Operation(summary = "업무 관리자 사용자 샘플 조회", description = "ADM 공통 권한 체계와 분리된 업무 관리자 사용자 예시를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findAdminUsers() {
        return ResponseEntity.ok(sampleService.findAdminUsers());
    }

    @GetMapping("/menus")
    @CpfTransaction(id = "BIZ01MNU0001", name = "BizAdmMenuList")
    @Operation(summary = "업무 관리자 메뉴 샘플 조회", description = "업무 모듈 메뉴와 버튼 권한 연결 예시를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findMenus() {
        return ResponseEntity.ok(sampleService.findMenus());
    }

    @GetMapping("/roles")
    @CpfTransaction(id = "BIZ01ROL0001", name = "BizAdmRoleList")
    @Operation(summary = "업무 관리자 역할 샘플 조회", description = "업무 관리자 역할과 권한 범위 예시를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findRoles() {
        return ResponseEntity.ok(sampleService.findRoles());
    }

    @GetMapping("/permissions")
    @CpfTransaction(id = "BIZ01PER0001", name = "BizAdmPermissionList")
    @Operation(summary = "업무 관리자 권한 샘플 조회", description = "메뉴/버튼/API 권한 매트릭스 예시를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findPermissions() {
        return ResponseEntity.ok(sampleService.findPermissions());
    }

    @GetMapping("/customers")
    @CpfTransaction(id = "BIZ01CUS0001", name = "BizAdmCustomerList")
    @Operation(summary = "고객 샘플 조회", description = "마스킹이 필요한 업무 데이터 목록 예시를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findCustomers() {
        return ResponseEntity.ok(sampleService.findCustomers(false, ""));
    }

    @PostMapping("/masking/unmask")
    @CpfTransaction(id = "BIZ02MSK0001", name = "BizAdmUnmask")
    @Operation(summary = "마스킹 해제 샘플", description = "마스킹 해제 권한과 감사 사유가 필요한 업무 운영 흐름을 보여줍니다.")
    public ResponseEntity<List<Map<String, Object>>> unmaskCustomers(
            @RequestParam String reason,
            @RequestParam(defaultValue = "admin-ui") String requestUser) {
        return ResponseEntity.ok(sampleService.findCustomers(true, requestUser + ":" + reason));
    }

    @GetMapping("/products")
    @CpfTransaction(id = "BIZ01PRD0001", name = "BizAdmProductList")
    @Operation(summary = "상품 샘플 조회", description = "업무 기준정보 관리 화면 샘플 데이터를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findProducts() {
        return ResponseEntity.ok(sampleService.findProducts());
    }

    @GetMapping("/orders")
    @CpfTransaction(id = "BIZ01ORD0001", name = "BizAdmOrderList")
    @Operation(summary = "주문 샘플 조회", description = "업무 거래 데이터 조회와 다운로드 대상 예시를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findOrders() {
        return ResponseEntity.ok(sampleService.findOrders());
    }

    @GetMapping("/settings")
    @CpfTransaction(id = "BIZ01SET0001", name = "BizAdmSettingList")
    @Operation(summary = "업무 설정 샘플 조회", description = "업무 모듈 전용 설정 관리 예시를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findSettings() {
        return ResponseEntity.ok(sampleService.findSettings());
    }

    @GetMapping("/downloads")
    @CpfTransaction(id = "BIZ01DWN0001", name = "BizAdmDownloadPolicyList")
    @Operation(summary = "업무 다운로드 정책 샘플 조회", description = "다운로드 권한, 마스킹, 감사 사유 정책 예시를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findDownloadPolicies() {
        return ResponseEntity.ok(sampleService.findDownloadPolicies());
    }
}
