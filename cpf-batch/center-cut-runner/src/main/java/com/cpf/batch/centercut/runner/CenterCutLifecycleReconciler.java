package com.cpf.batch.centercut.runner;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Drain이 끝난 실행을 PAUSED로 확정하고 Terminal 상태가 Runner 완료에 의해 덮이지 않도록 보조합니다. */
@Component
public class CenterCutLifecycleReconciler {
    private final JdbcTemplate jdbc;
    public CenterCutLifecycleReconciler(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Scheduled(fixedDelayString = "${cpf.center-cut.lifecycle-reconcile-ms:1000}")
    public void reconcile() {
        jdbc.update("""
            UPDATE bat_center_cut_execution e
               SET e.execution_state='PAUSED',e.updated_at=CURRENT_TIMESTAMP(6)
             WHERE e.execution_state='DRAINING'
               AND NOT EXISTS (
                   SELECT 1
                     FROM bat_center_cut_item i
                     JOIN bat_center_cut_claim c ON c.center_cut_item_id=i.center_cut_item_id
                    WHERE i.center_cut_execution_id=e.center_cut_execution_id
                      AND c.claim_status IN ('CLAIMED','RUNNING')
                      AND c.lease_until>=CURRENT_TIMESTAMP(6)
               )
            """);
    }
}
