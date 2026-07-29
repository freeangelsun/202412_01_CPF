package com.cpf.batch.control.deploy;

import com.cpf.batch.api.*;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;

@Repository
public class DeploymentExecutionRepository {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;

    public DeploymentExecutionRepository(JdbcTemplate jdbc, CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = jdbc;
        this.sql = sqlCatalogProvider.forModule("bat");
    }

    @Transactional
    public Optional<Map<String,Object>> begin(DeploymentRequest r) {
        try {
            jdbc.update(sql.required("deploy-execution-begin"),
              r.deploymentId(),r.manifest().cellId(),r.idempotencyKey(),r.manifest().artifact().version(),
              r.manifest().deployment().strategy().name(),r.requestedBy(),r.approvedBy(),r.reason());
            return Optional.empty();
        } catch(DuplicateKeyException duplicate) {
            return findByIdempotency(r.idempotencyKey());
        }
    }

    public void instance(String deploymentId,int sequence,DeploymentResult.InstanceResult result) {
        jdbc.update(sql.required("deploy-execution-instance-result"),
                deploymentId,sequence,result.instanceId(),result.stage(),result.state().name(),
                SensitiveTextSanitizer.sanitize(result.message()));
    }

    public void finish(String deploymentId,CommandState state,String failureStage,String message) {
        jdbc.update(sql.required("deploy-execution-finish"),
                state.name(),failureStage,SensitiveTextSanitizer.sanitize(message),deploymentId);
    }

    public Optional<Map<String,Object>> findByIdempotency(String key) {
        return jdbc.queryForList(
                sql.required("deploy-execution-find-idempotency"),key).stream().findFirst();
    }
}
