package com.cpf.batch.centercut.runner;

import com.cpf.batch.centercut.runner.internal.JdbcCenterCutClaimRepository;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Process-kill 이후 만료된 Claim을 UNKNOWN으로 회수하고 Drain 완료 실행을 PAUSED로 확정합니다.
 */
@Component
public class CenterCutLifecycleReconciler {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;
    private final JdbcCenterCutClaimRepository claims;

    public CenterCutLifecycleReconciler(
            JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider sqlCatalogProvider,
            JdbcCenterCutClaimRepository claims) {
        this.jdbc = jdbc;
        this.sql = sqlCatalogProvider.forModule("bat");
        this.claims = claims;
    }

    /**
     * 새 Step 실행이 없어도 Process-kill로 남은 만료 Claim을 주기적으로 회수합니다.
     */
    @Scheduled(fixedDelayString = "${cpf.center-cut.claim-recovery-ms:1000}")
    public void recoverExpiredClaims() {
        claims.recoverExpiredToUnknown();
    }

    /**
     * Drain이 끝난 실행을 PAUSED로 확정해 Runner 완료가 Terminal 상태를 덮지 못하게 합니다.
     */
    @Scheduled(fixedDelayString = "${cpf.center-cut.lifecycle-reconcile-ms:1000}")
    public void reconcile() {
        jdbc.update(sql.required("centercut-lifecycle-mark-drained-paused"));
    }
}
