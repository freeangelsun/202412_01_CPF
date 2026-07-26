package com.cpf.batch.centercut.runner.internal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Repository
public class JdbcCenterCutAdmissionControl {
    private final JdbcTemplate jdbc; public JdbcCenterCutAdmissionControl(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @Transactional
    public boolean acquire(String executionId,int tpsLimit,int concurrencyLimit){
        // Serialize admission for one execution so concurrent runners cannot both pass the same concurrency/TPS snapshot.
        jdbc.queryForObject("SELECT center_cut_execution_id FROM bat_center_cut_execution WHERE center_cut_execution_id=? FOR UPDATE",String.class,executionId);
        Integer active=jdbc.queryForObject("""
          SELECT COUNT(*) FROM bat_center_cut_claim c JOIN bat_center_cut_item i ON i.center_cut_item_id=c.center_cut_item_id
          WHERE i.center_cut_execution_id=? AND c.claim_status IN ('CLAIMED','RUNNING') AND c.lease_until>=CURRENT_TIMESTAMP(6)
          """,Integer.class,executionId);
        if(active!=null&&active>=Math.max(1,concurrencyLimit))return false;
        if(tpsLimit<=0)return true;
        long second=Instant.now().getEpochSecond();
        jdbc.update("INSERT IGNORE INTO bat_center_cut_rate_window(center_cut_execution_id,window_second,admitted_count) VALUES(?,?,0)",executionId,second);
        return jdbc.update("""
          UPDATE bat_center_cut_rate_window SET admitted_count=admitted_count+1,updated_at=CURRENT_TIMESTAMP(6)
          WHERE center_cut_execution_id=? AND window_second=? AND admitted_count<?
          """,executionId,second,tpsLimit)==1;
    }
}
