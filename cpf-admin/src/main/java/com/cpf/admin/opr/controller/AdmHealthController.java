package com.cpf.admin.opr.controller;

import com.cpf.admin.config.AdmPersistencePolicy;
import com.cpf.foundation.runtime.CpfInstanceIdentity;
import com.cpf.platform.operations.observability.internal.logging.CpfTransactionContextAnomalyMonitor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** ADM Liveness/Readiness API. 필수 Dependency 장애를 HTTP 200으로 숨기지 않습니다. */
@RestController
@RequestMapping("/adm/api/health")
@Tag(name = "ADM-Health", description = "ADM liveness/readiness API")
public class AdmHealthController extends com.cpf.admin.common.base.AdmBaseController {
    private final JdbcTemplate admJdbcTemplate;
    private final JdbcTemplate cpfJdbcTemplate;
    private final Environment environment;
    private final AdmPersistencePolicy persistencePolicy;

    public AdmHealthController(
            @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate,
            @Qualifier("cpfJdbcTemplate") JdbcTemplate cpfJdbcTemplate,
            Environment environment,
            AdmPersistencePolicy persistencePolicy) {
        this.admJdbcTemplate = admJdbcTemplate;
        this.cpfJdbcTemplate = cpfJdbcTemplate;
        this.environment = environment;
        this.persistencePolicy = persistencePolicy;
    }

    @GetMapping("/liveness")
    @Operation(operationId = "getAdmLiveness", summary = "ADM Liveness 조회")
    public ResponseEntity<Map<String, Object>> liveness() {
        return ResponseEntity.ok(base("UP", Map.of("process", "UP")));
    }

    /**
     * 하나의 {@code @Operation} 으로 두 경로를 매핑하면 springdoc 이 중복을 피하려고
     * {@code getAdmReadiness_1} 처럼 접미사를 붙이고, 그 값은 CPF 정본 operationId 규격
     * (밑줄+숫자 접미사 금지)에 걸려 Runtime OpenAPI 계약 검증이 실패한다. 그래서 경로를
     * 분리한다. 공개 계약(cpf-admin/frontend/openapi/cpf-openapi.json)은
     * {@code GET /adm/api/health = getAdmReadiness} 하나만 선언하므로 그 대응을 그대로 유지한다.
     */
    @GetMapping("")
    @Operation(operationId = "getAdmReadiness", summary = "ADM Readiness 조회")
    public ResponseEntity<Map<String, Object>> readiness() {
        return readinessState();
    }

    /**
     * {@code /readiness} 는 Infra probe 용 별칭 경로다
     * ({@code AdmApiAuthFilter.isPublicHealthRequest} 가 공개로 선언한다).
     * 공개 API 계약에는 없는 경로이므로 OpenAPI 문서에서는 감춘다 — 노출하면 정본 계약과
     * Runtime 문서가 어긋나 parity 검증이 실패한다.
     */
    @GetMapping("/readiness")
    @Operation(hidden = true)
    public ResponseEntity<Map<String, Object>> readinessAlias() {
        return readinessState();
    }

    private ResponseEntity<Map<String, Object>> readinessState() {
        Map<String, Object> checks = new LinkedHashMap<>();
        checks.put("admDataStore", checkDatabase(admJdbcTemplate));
        checks.put("cpfDB", checkDatabase(cpfJdbcTemplate));
        boolean admDbUp = "UP".equals(checks.get("admDataStore"));
        String sessionStore = persistencePolicy.memoryEnabled() ? "MEMORY" : (admDbUp ? "UP" : "DOWN");
        checks.put("sessionStore", sessionStore);
        boolean sessionReady = persistencePolicy.memoryEnabled() || "UP".equals(sessionStore);
        boolean ready = sessionReady && "UP".equals(checks.get("cpfDB"));
        Map<String, Object> response = base(ready ? "UP" : "DOWN", checks);
        response.put("dataSourceMode", persistencePolicy.mode().name());
        response.put("fallbackActive", persistencePolicy.memoryEnabled() && !admDbUp);
        response.put("degraded", persistencePolicy.memoryEnabled() && !admDbUp);
        response.put("reasonCode", !sessionReady ? "ADM_SESSION_STORE_UNAVAILABLE"
                : persistencePolicy.memoryEnabled() && !admDbUp ? "ADM_DB_UNAVAILABLE_MEMORY_MODE" : null);
        response.put("alertRequired", !sessionReady);
        response.put("transactionContextMissingCount", CpfTransactionContextAnomalyMonitor.missingCount());
        return ResponseEntity.status(ready ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    private Map<String, Object> base(String status, Map<String, Object> checks) {
        Map<String, Object> response = new LinkedHashMap<>();
        CpfInstanceIdentity.Identity identity = CpfInstanceIdentity.current();
        response.put("status", status);
        response.put("service", "ADM");
        response.put("moduleId", environment.getProperty("cpf.framework.module-id", "ADM"));
        response.put("wasId", environment.getProperty("cpf.framework.was-id", "admAP01"));
        response.put("instanceId", identity.instanceId());
        response.put("host", identity.hostName());
        response.put("hostName", identity.hostName());
        response.put("processId", identity.processId());
        response.put("profiles", environment.getActiveProfiles());
        response.put("checkedAt", OffsetDateTime.now().toString());
        response.put("checks", checks);
        return response;
    }

    private String checkDatabase(JdbcTemplate jdbcTemplate) {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1", Integer.class);
            return Integer.valueOf(1).equals(result) ? "UP" : "DOWN";
        } catch (Exception ex) {
            return "DOWN";
        }
    }
}
