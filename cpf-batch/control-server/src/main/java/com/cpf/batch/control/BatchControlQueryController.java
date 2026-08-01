package com.cpf.batch.control;

import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.data.CpfDataRow;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/** BAT Control Server의 실행·Agent·Lease·Recovery 운영 조회 API입니다. */
@RestController
@RequestMapping("/api/v1/batch/views")
public class BatchControlQueryController {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;

    public BatchControlQueryController(JdbcTemplate jdbc, CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = jdbc;
        this.sql = sqlCatalogProvider.forModule("bat");
    }

    @GetMapping("/{view}")
    public CpfDataRow view(@PathVariable String view) {
        String statementKey = switch (view) {
            case "overview" -> "control-view-overview";
            case "instances" -> "control-view-instances";
            case "scheduler" -> "control-view-scheduler";
            case "worker-pools" -> "control-view-worker-pools";
            case "center-cut" -> "control-view-centercut";
            case "agents" -> "control-view-agents";
            case "job-packs" -> "control-view-job-packs";
            case "executions" -> "control-view-executions";
            case "deployments" -> "control-view-deployments";
            case "recovery" -> "control-view-recovery";
            case "leases" -> "control-view-leases";
            case "alerts" -> "control-view-alerts";
            case "audit" -> "control-view-audit";
            default -> throw new IllegalArgumentException("Unsupported BAT control view: " + view);
        };
        return CpfDataRow.of("view", view, "items", jdbc.queryForList(sql.required(statementKey)));
    }
}
