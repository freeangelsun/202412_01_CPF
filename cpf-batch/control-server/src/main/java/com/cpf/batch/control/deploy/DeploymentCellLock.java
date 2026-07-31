package com.cpf.batch.control.deploy;

import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 배포 Cell lock 저장소입니다. Lock 경합은 명시적인 결과로 반환하고 DB/Resource 장애는 예외로
 * 전파하여 경합으로 위장하지 않습니다.
 */
@Component
public class DeploymentCellLock {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;

    @Autowired
    public DeploymentCellLock(
            JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this(jdbc, sqlCatalogProvider.forModule("bat"));
    }

    DeploymentCellLock(JdbcTemplate jdbc, CpfVendorSqlCatalog sql) {
        this.jdbc = jdbc;
        this.sql = sql;
    }

    public Acquisition acquire(String cellId, String deploymentId) {
        jdbc.update(sql.required("deploy-lock-acquire"), cellId, deploymentId);
        String owner = jdbc.queryForObject(
                sql.required("deploy-lock-owner"),
                String.class,
                cellId);
        return deploymentId.equals(owner) ? Acquisition.ACQUIRED : Acquisition.CONTENDED;
    }

    public String owner(String cellId) {
        return jdbc.queryForObject(sql.required("deploy-lock-owner"), String.class, cellId);
    }

    public void release(String cellId, String deploymentId) {
        int changed = jdbc.update(sql.required("deploy-lock-release"), cellId, deploymentId);
        if (changed == 1) return;
        String currentOwner = owner(cellId);
        if (currentOwner == null || currentOwner.isBlank()) return;
        if (!deploymentId.equals(currentOwner)) {
            throw new SecurityException("Deployment lock is owned by another execution");
        }
        throw new IllegalStateException("Deployment lock release was not persisted");
    }

    public enum Acquisition {
        ACQUIRED,
        CONTENDED
    }
}
