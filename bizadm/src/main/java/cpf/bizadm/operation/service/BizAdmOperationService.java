package cpf.bizadm.operation.service;

import cpf.bizadm.operation.repository.BizAdmOperationRepository;
import cpf.cmn.utils.MaskingUtils;
import cpf.cmn.utils.TextUtils;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BIZADM 업무 운영 서비스입니다.
 *
 * <p>조회 데이터는 DB 저장소에서 읽고, 개인정보 원문 조회는 감사 사유를 필수로 남깁니다.
 * 업무 관리자 기능은 ADM 공통 운영자 기능과 분리하되, 운영 화면에서 함께 연결할 수 있도록
 * 동일한 응답 구조와 감사 기준을 사용합니다.</p>
 */
@Service
public class BizAdmOperationService {
    private final BizAdmOperationRepository repository;

    public BizAdmOperationService(BizAdmOperationRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> findAdminUsers() {
        return repository.findAdminUsers();
    }

    public List<Map<String, Object>> findMenus() {
        return repository.findMenus();
    }

    public List<Map<String, Object>> findRoles() {
        return repository.findRoles();
    }

    public List<Map<String, Object>> findPermissions() {
        return repository.findPermissions();
    }

    /**
     * 고객 목록은 기본적으로 마스킹해서 반환합니다.
     */
    public List<Map<String, Object>> findCustomers() {
        return repository.findCustomers().stream()
                .map(this::maskedCustomer)
                .toList();
    }

    /**
     * 고객 원문 조회는 사유와 요청자를 감사 로그에 남긴 뒤 반환합니다.
     */
    public List<Map<String, Object>> unmaskCustomers(String reason, String requestUser) {
        String resolvedReason = TextUtils.requireText(reason, "reason");
        String resolvedUser = TextUtils.defaultIfBlank(requestUser, "BIZADM_OPERATOR");
        return repository.findCustomers().stream()
                .map(row -> {
                    repository.insertMaskingAudit(String.valueOf(row.get("customerNo")), resolvedUser, resolvedReason, "SUCCESS");
                    Map<String, Object> result = new LinkedHashMap<>(row);
                    result.put("unmaskAuditReason", resolvedReason);
                    result.put("unmaskRequestUser", resolvedUser);
                    return result;
                })
                .toList();
    }

    public List<Map<String, Object>> findProducts() {
        return repository.findProducts();
    }

    public List<Map<String, Object>> findOrders() {
        return repository.findOrders();
    }

    public List<Map<String, Object>> findSettings() {
        return repository.findSettings();
    }

    public List<Map<String, Object>> findDownloadPolicies() {
        return repository.findDownloadPolicies();
    }

    private Map<String, Object> maskedCustomer(Map<String, Object> row) {
        Map<String, Object> masked = new LinkedHashMap<>(row);
        masked.put("email", MaskingUtils.maskEmail(String.valueOf(row.get("email"))));
        masked.put("mobileNo", MaskingUtils.maskMobile(String.valueOf(row.get("mobileNo"))));
        return masked;
    }
}
