package com.cpf.admin.opr.controller;

import com.cpf.admin.config.AdmPersistencePolicy;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.platform.operations.api.runtime.CpfInstanceIdentity;
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

    @GetMapping({"", "/readiness"})
    @Operation(operationId = "getAdmReadiness", summary = "ADM Readiness 조회")
    public ResponseEntity<Map<String, Object>> readiness() {
        Map<String, Object> checks = new LinkedHashMap<>();
        checks.put("admDB", checkDatabase(admJdbcTemplate));
        checks.put("cpfDB", checkDatabase(cpfJdbcTemplate));
        boolean admDbUp = "UP".equals(checks.get("admDB"));
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
        response.put("transactionContextMissingCount", CpfContexts.missingCount());
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
