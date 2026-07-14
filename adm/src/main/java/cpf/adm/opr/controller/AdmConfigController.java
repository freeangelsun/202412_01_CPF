package cpf.adm.opr.controller;

import cpf.adm.opr.service.AdmAuditLogService;
import cpf.cmn.cfg.dto.CommonConfigRequest;
import cpf.cmn.cfg.service.ConfigCacheService;
import cpf.pfw.common.logging.CpfTransaction;
import cpf.pfw.common.logging.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/adm/api/configs")
@Tag(name = "ADM-PFW Configs", description = "PFW 공통 설정 관리 API")
public class AdmConfigController {
    private final ConfigCacheService configCacheService;
    private final AdmAuditLogService auditLogService;

    public AdmConfigController(ConfigCacheService configCacheService, AdmAuditLogService auditLogService) {
        this.configCacheService = configCacheService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    @CpfTransaction(id = "ADM01CFG0010", name = "ADMConfigList")
    @Operation(operationId = "admConfigFindConfigs", summary = "공통 설정 목록 조회", description = "pfw_config 기준 설정을 조회하며 암호화 항목 값은 마스킹합니다.")
    public ResponseEntity<List<Map<String, Object>>> findConfigs() {
        return ResponseEntity.ok(configCacheService.getAllConfigs().stream().map(this::maskSecret).toList());
    }

    @GetMapping("/{configId}")
    @CpfTransaction(id = "ADM01CFG0011", name = "ADMConfigDetail")
    @Operation(operationId = "admConfigFindConfig", summary = "공통 설정 상세 조회", description = "설정 ID로 pfw_config 상세 정보를 조회하며 암호화 항목 값은 마스킹합니다.")
    public ResponseEntity<Map<String, Object>> findConfig(@PathVariable Long configId) {
        return ResponseEntity.ok(maskSecret(configCacheService.getConfigById(configId)));
    }

    @PostMapping
    @CpfTransaction(id = "ADM02CFG0012", name = "ADMConfigCreate")
    @Operation(operationId = "admConfigCreateConfig", summary = "공통 설정 등록", description = "pfw_config에 신규 설정을 등록하고 설정 캐시를 갱신합니다.")
    public ResponseEntity<Map<String, Object>> createConfig(
            @Valid @RequestBody CommonConfigRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.getReason());
        Map<String, Object> created = configCacheService.createConfig(request);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, request.getRequestUser()),
                "CONFIG_CREATE",
                "pfw_config",
                String.valueOf(created.getOrDefault("configId", request.getConfigKey())),
                reason,
                null,
                String.valueOf(maskSecret(created)),
                String.valueOf(maskSecret(created)),
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(maskSecret(created));
    }

    @PutMapping("/{configId}")
    @CpfTransaction(id = "ADM03CFG0013", name = "ADMConfigUpdate")
    @Operation(operationId = "admConfigUpdateConfig", summary = "공통 설정 수정", description = "pfw_config를 수정하고 설정 캐시를 갱신합니다.")
    public ResponseEntity<Map<String, Object>> updateConfig(
            @PathVariable Long configId,
            @Valid @RequestBody CommonConfigRequest request,
            HttpServletRequest servletRequest) {
        String reason = auditLogService.requireReason(request.getReason());
        Map<String, Object> before = configCacheService.getConfigById(configId);
        Map<String, Object> updated = configCacheService.updateConfig(configId, request);
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, request.getRequestUser()),
                "CONFIG_UPDATE",
                "pfw_config",
                String.valueOf(configId),
                reason,
                String.valueOf(maskSecret(before)),
                String.valueOf(maskSecret(updated)),
                "설정 수정",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(maskSecret(updated));
    }

    @DeleteMapping("/{configId}")
    @CpfTransaction(id = "ADM04CFG0014", name = "ADMConfigDisable")
    @Operation(operationId = "admConfigDeleteConfig", summary = "공통 설정 비활성", description = "pfw_config를 비활성화하고 설정 캐시를 갱신합니다.")
    public ResponseEntity<List<Map<String, Object>>> deleteConfig(
            @PathVariable Long configId,
            @RequestParam String reason,
            @RequestParam(defaultValue = "SYSTEM") String requestUser,
            HttpServletRequest servletRequest) {
        String requiredReason = auditLogService.requireReason(reason);
        Map<String, Object> before = configCacheService.getConfigById(configId);
        List<Map<String, Object>> latest = configCacheService.deleteConfig(configId).stream().map(this::maskSecret).toList();
        auditLogService.record(
                TransactionContext.getOrCreateTransactionId(),
                requestUser(servletRequest, requestUser),
                "CONFIG_DISABLE",
                "pfw_config",
                String.valueOf(configId),
                requiredReason,
                String.valueOf(maskSecret(before)),
                null,
                "설정 비활성",
                servletRequest.getRemoteAddr());
        return ResponseEntity.ok(latest);
    }

    private Map<String, Object> maskSecret(Map<String, Object> source) {
        if (source == null) {
            return Map.of();
        }
        Map<String, Object> masked = new LinkedHashMap<>(source);
        Object encrypted = firstValue(masked, "encryptedYn", "encrypted_yn", "ENCRYPTED_YN");
        if ("Y".equalsIgnoreCase(String.valueOf(encrypted))) {
            masked.computeIfPresent("configValue", (key, value) -> "********");
            masked.computeIfPresent("config_value", (key, value) -> "********");
            masked.computeIfPresent("CONFIG_VALUE", (key, value) -> "********");
        }
        return masked;
    }

    private Object firstValue(Map<String, Object> source, String... keys) {
        for (String key : keys) {
            if (source.containsKey(key)) {
                return source.get(key);
            }
        }
        return "";
    }

    private String requestUser(HttpServletRequest request, String fallback) {
        Object operatorId = request.getAttribute("adm.operatorId");
        if (operatorId instanceof String value && !value.isBlank()) {
            return value;
        }
        return fallback;
    }
}
