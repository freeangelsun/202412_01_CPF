package cpf.bizadm.sample.service;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 업무 관리자 샘플 데이터를 제공합니다.
 *
 * <p>실제 프로젝트에서는 이 서비스 뒤에 업무 DB, ADM 공통 권한 검사, 다운로드 감사 로그를 연결합니다.
 * 샘플은 프레임워크 배포본에서 개발자가 경계를 이해할 수 있도록 읽기 전용 데이터로 유지합니다.</p>
 */
@Service
public class BizAdmSampleService {

    public List<Map<String, Object>> findAdminUsers() {
        return List.of(
                Map.of("adminUserId", "biz-admin-01", "name", "업무 관리자", "roleId", "BIZ_MANAGER"),
                Map.of("adminUserId", "biz-viewer-01", "name", "업무 조회자", "roleId", "BIZ_VIEWER"));
    }

    public List<Map<String, Object>> findMenus() {
        return List.of(
                Map.of("menuId", "BIZ_CUSTOMER", "menuName", "고객 관리", "moduleId", "BIZ"),
                Map.of("menuId", "BIZ_ORDER", "menuName", "주문 관리", "moduleId", "BIZ"));
    }

    public List<Map<String, Object>> findRoles() {
        return List.of(
                Map.of("roleId", "BIZ_MANAGER", "roleName", "업무 관리자", "writeAllowed", true),
                Map.of("roleId", "BIZ_VIEWER", "roleName", "업무 조회자", "writeAllowed", false));
    }

    public List<Map<String, Object>> findPermissions() {
        return List.of(
                Map.of("roleId", "BIZ_MANAGER", "target", "BIZ_CUSTOMER", "button", "SAVE", "allowYn", "Y"),
                Map.of("roleId", "BIZ_VIEWER", "target", "BIZ_CUSTOMER", "button", "SAVE", "allowYn", "N"));
    }

    public List<Map<String, Object>> findCustomers(boolean unmasked, String auditReason) {
        String email = unmasked ? "customer01@example.com" : "cu****@example.com";
        String mobile = unmasked ? "010-1234-5678" : "010-****-5678";
        return List.of(Map.of(
                "customerNo", "C000000001",
                "customerName", "샘플 고객",
                "email", email,
                "mobileNo", mobile,
                "unmaskAuditReason", auditReason));
    }

    public List<Map<String, Object>> findProducts() {
        return List.of(Map.of("productId", "P0001", "productName", "샘플 상품", "useYn", "Y"));
    }

    public List<Map<String, Object>> findOrders() {
        return List.of(Map.of("orderId", "O0001", "customerNo", "C000000001", "amount", 12000));
    }

    public List<Map<String, Object>> findSettings() {
        return List.of(Map.of("settingKey", "BIZ.ORDER.APPROVAL_REQUIRED", "settingValue", "Y"));
    }

    public List<Map<String, Object>> findDownloadPolicies() {
        return List.of(Map.of(
                "downloadType", "BIZ_CUSTOMER",
                "reasonRequiredYn", "Y",
                "maskingRequiredYn", "Y",
                "unmaskPermissionCode", "BIZ_CUSTOMER_UNMASK"));
    }
}
