package com.cpf.admin.opr.controller;

import com.cpf.core.common.logging.CpfTransactionContextAnomalyMonitor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ADM 기동 검증과 smoke 자동화를 위한 health API입니다.
 *
 * <p>actuator 의존성을 강제하지 않고도 로컬/CI smoke가 ADM, CPF, MBR datasource
 * 연결 상태를 확인할 수 있게 합니다. 운영 보안상 민감한 접속 정보는 반환하지 않습니다.</p>
 */
@RestController
@RequestMapping("/adm/api/health")
@Tag(name = "ADM-Health", description = "ADM health and smoke readiness API")
public class AdmHealthController extends com.cpf.admin.common.base.AdmBaseController {
    private final JdbcTemplate admJdbcTemplate;
    private final JdbcTemplate cpfJdbcTemplate;
    private final JdbcTemplate mbrJdbcTemplate;

    public AdmHealthController(
            @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate,
            @Qualifier("cpfJdbcTemplate") JdbcTemplate cpfJdbcTemplate,
            @Qualifier("mbrJdbcTemplate") JdbcTemplate mbrJdbcTemplate) {
        this.admJdbcTemplate = admJdbcTemplate;
        this.cpfJdbcTemplate = cpfJdbcTemplate;
        this.mbrJdbcTemplate = mbrJdbcTemplate;
    }

    @GetMapping
    @Operation(
            operationId = "getAdmHealth",
            summary = "ADM health 조회",
            description = "ADM 앱 기동 상태, 운영 datasource, 거래 context 누락 누적 건수를 확인합니다.")
    public Map<String, Object> health() {
        Map<String, Object> checks = new LinkedHashMap<>();
        checks.put("admDB", checkDatabase(admJdbcTemplate));
        checks.put("cpfDB", checkDatabase(cpfJdbcTemplate));
        checks.put("mbrDB", checkDatabase(mbrJdbcTemplate));

        boolean up = checks.values().stream().allMatch("UP"::equals);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", up ? "UP" : "DEGRADED");
        response.put("service", "ADM");
        response.put("checkedAt", OffsetDateTime.now().toString());
        response.put("checks", checks);
        response.put("transactionContextMissingCount", CpfTransactionContextAnomalyMonitor.missingCount());
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
