package com.cpf.batch.centercut.runner.internal;

import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;

@Repository
public class JdbcCenterCutAdmissionControl {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;

    public JdbcCenterCutAdmissionControl(JdbcTemplate jdbc, CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = jdbc;
        this.sql = sqlCatalogProvider.forModule("bat");
    }

    @Transactional
    public boolean acquire(String executionId,int tpsLimit,int concurrencyLimit){
        // Serialize admission for one execution so concurrent runners cannot both pass the same concurrency/TPS snapshot.
        jdbc.queryForObject(sql.required("centercut-admission-lock-execution"),
                String.class,executionId);
        Integer active=jdbc.queryForObject(sql.required("centercut-admission-count-active"),
                Integer.class,executionId);
        if(active!=null&&active>=Math.max(1,concurrencyLimit))return false;
        if(tpsLimit<=0)return true;
        long second=Instant.now().getEpochSecond();
        jdbc.update(sql.required("centercut-admission-insert-rate-window-idempotent"),
                executionId,second);
        return jdbc.update(sql.required("centercut-admission-increment-rate-window"),
                executionId,second,tpsLimit)==1;
    }
}
