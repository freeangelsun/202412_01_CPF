package com.cpf.batch.centercut.runner;

import com.cpf.core.common.database.CpfVendorSqlCatalog;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Drain이 끝난 실행을 PAUSED로 확정하고 Terminal 상태가 Runner 완료에 의해 덮이지 않도록 보조합니다. */
@Component
public class CenterCutLifecycleReconciler {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;

    public CenterCutLifecycleReconciler(JdbcTemplate jdbc, Environment environment) {
        this.jdbc = jdbc;
        this.sql = CpfVendorSqlCatalog.create(environment, "bat");
    }

    @Scheduled(fixedDelayString = "${cpf.center-cut.lifecycle-reconcile-ms:1000}")
    public void reconcile() {
        jdbc.update(sql.required("centercut-lifecycle-mark-drained-paused"));
    }
}
