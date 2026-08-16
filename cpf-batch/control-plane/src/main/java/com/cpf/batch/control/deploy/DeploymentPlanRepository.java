package com.cpf.batch.control.deploy;

import com.cpf.batch.api.DeploymentCellManifest;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

@Repository
public class DeploymentPlanRepository {
    private final JdbcTemplate jdbc;
    private final ObjectMapper mapper;
    private final CpfVendorSqlCatalog sql;

    public DeploymentPlanRepository(
            JdbcTemplate jdbc,
            ObjectMapper mapper,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = jdbc;
        this.mapper = mapper;
        this.sql = sqlCatalogProvider.forModule("bat");
    }

    @Transactional
    public Plan create(
            String planId,
            DeploymentCellManifest manifest,
            String user,
            String reason) throws Exception {
        String json = mapper.writeValueAsString(manifest);
        String hash = HexFormat.of().formatHex(
                MessageDigest.getInstance("SHA-256")
                        .digest(json.getBytes(StandardCharsets.UTF_8)));
        jdbc.update(
                sql.required("deploy-plan-insert"),
                planId,
                manifest.cellId(),
                json,
                hash,
                user,
                reason);
        jdbc.update(
                sql.required("deploy-cell-upsert"),
                manifest.cellId(),
                manifest.environment(),
                manifest.runtimeRole().name(),
                manifest.serviceId(),
                manifest.artifact().version(),
                hash,
                manifest.desiredState().name());
        jdbc.update(sql.required("deploy-instances-delete"), manifest.cellId());
        for (var instance : manifest.instances()) {
            jdbc.update(
                    sql.required("deploy-instance-insert"),
                    manifest.cellId(),
                    instance.instanceId(),
                    instance.hostAlias(),
                    instance.port(),
                    instance.profile(),
                    instance.zone(),
                    instance.pool(),
                    instance.agentBaseUrl(),
                    instance.configRef(),
                    manifest.desiredState().name());
        }
        return new Plan(planId, manifest.cellId(), hash, "PLANNED");
    }

    public DeploymentCellManifest load(String planId) throws Exception {
        String json = jdbc.queryForObject(
                sql.required("deploy-plan-load"),
                String.class,
                planId);
        return mapper.readValue(json, DeploymentCellManifest.class);
    }

    public void mark(String planId, String state) {
        jdbc.update(sql.required("deploy-plan-mark"), state, planId);
    }

    public record Plan(String planId, String cellId, String manifestHash, String state) {
    }
}
