package com.cpf.admin.opr.controller;

import com.cpf.core.api.admin.CpfOwnerAdminOperationsPort;
import com.cpf.core.api.admin.CpfOwnerAdminQuery;
import com.cpf.core.api.logging.CpfTransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
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
    private final CpfOwnerAdminOperationsPort mbrOperations;

    public AdmHealthController(
            @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate,
            @Qualifier("cpfJdbcTemplate") JdbcTemplate cpfJdbcTemplate,
            @Qualifier("mbrOwnerAdminOperationsPort") CpfOwnerAdminOperationsPort mbrOperations) {
        this.admJdbcTemplate = admJdbcTemplate;
        this.cpfJdbcTemplate = cpfJdbcTemplate;
        this.mbrOperations = mbrOperations;
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
        checks.put("mbrOwner", checkMbrOwner());
        boolean ready = checks.values().stream().allMatch("UP"::equals);
        Map<String, Object> response = base(ready ? "UP" : "DOWN", checks);
        response.put("transactionContextMissingCount", CpfTransactionContext.missingCount());
        return ResponseEntity.status(ready ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    private Map<String, Object> base(String status, Map<String, Object> checks) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", status);
        response.put("service", "ADM");
        response.put("checkedAt", OffsetDateTime.now().toString());
        response.put("checks", checks);
        return response;
    }

    private String checkMbrOwner() {
        try {
            Map<String, Object> response = mbrOperations.query(new CpfOwnerAdminQuery("system", "health", null, Map.of()));
            return "UP".equals(String.valueOf(response.get("status"))) ? "UP" : "DOWN";
        } catch (RuntimeException ex) {
            return "DOWN";
        }
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
