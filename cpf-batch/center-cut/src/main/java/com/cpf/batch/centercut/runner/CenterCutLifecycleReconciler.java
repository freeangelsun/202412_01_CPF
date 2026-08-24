package com.cpf.batch.centercut.runner;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Drain 완료 실행을 PAUSED로 확정합니다. 만료 Claim 회수는 실제 DB Worker가 소유합니다.
 */
@Component
public class CenterCutLifecycleReconciler {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;

    public CenterCutLifecycleReconciler(
            JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = jdbc;
        this.sql = sqlCatalogProvider.forModule("bat");
    }

    /**
     * Drain이 끝난 실행을 PAUSED로 확정해 Runner 완료가 Terminal 상태를 덮지 못하게 합니다.
     */
    @Scheduled(fixedDelayString = "${cpf.center-cut.lifecycle-reconcile-ms:1000}")
    public void reconcile() {
        jdbc.update(sql.required("centercut-lifecycle-mark-drained-paused"));
    }
}
