package com.cpf.batch.control.deploy;

import com.cpf.batch.api.CommandState;
import com.cpf.batch.api.DeploymentRequest;
import com.cpf.batch.api.DeploymentResult;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** Deployment 실행·instance side-effect의 durable owner입니다. */
@Repository
public class DeploymentExecutionRepository {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;
    private final ObjectMapper canonicalJson;

    @Autowired
    public DeploymentExecutionRepository(
            JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider sqlCatalogProvider,
            ObjectMapper objectMapper) {
        this(jdbc, sqlCatalogProvider.forModule("bat"), objectMapper);
    }

    DeploymentExecutionRepository(JdbcTemplate jdbc, CpfVendorSqlCatalog sql, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.sql = sql;
        ObjectMapper canonicalMapper = objectMapper.copy();
        canonicalMapper.registerModule(new JavaTimeModule());
        canonicalMapper.setConfig(canonicalMapper.getSerializationConfig()
                .with(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY));
        canonicalMapper.enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS);
        this.canonicalJson = canonicalMapper;
    }

    @Transactional
    public Optional<Map<String, Object>> begin(DeploymentRequest request) {
        String scope = scope(request);
        String requestHash = requestHash(request);
        try {
            jdbc.update(sql.required("deploy-execution-begin"),
                    request.deploymentId(), request.manifest().cellId(), scope, request.idempotencyKey(), requestHash,
                    request.manifest().artifact().version(), request.manifest().deployment().strategy().name(),
                    request.expectedVersion(), request.approvalRequestId(), request.requestedBy(),
                    request.approvedBy(), request.reason());
            return Optional.empty();
        } catch (DuplicateKeyException duplicate) {
            Optional<Map<String, Object>> existing = findByIdempotency(scope, request.idempotencyKey());
            if (existing.isEmpty()) {
                throw new DeploymentIdempotencyConflictException(
                        "Deployment idempotency conflict exists but the prior execution cannot be loaded");
            }
            assertSameRequest(existing.get(), requestHash, request);
            return existing;
        }
    }

    public void instance(String deploymentId, int sequence, DeploymentResult.InstanceResult result) {
        int changed = jdbc.update(sql.required("deploy-execution-instance-result"),
                deploymentId, sequence, result.instanceId(), result.stage(), result.state().name(),
                SensitiveTextSanitizer.sanitize(result.message()));
        if (changed != 1) throw new IllegalStateException("Deployment instance result was not persisted");
    }

    public void finish(String deploymentId, CommandState state, String failureStage, String message) {
        int changed = jdbc.update(sql.required("deploy-execution-finish"),
                state.name(), failureStage, SensitiveTextSanitizer.sanitize(message), deploymentId);
        if (changed != 1) throw new IllegalStateException("Deployment execution final state was not persisted");
    }

    public Optional<Map<String, Object>> findByIdempotency(String scope, String key) {
        return jdbc.queryForList(sql.required("deploy-execution-find-idempotency"), scope, key).stream().findFirst();
    }

    public Optional<Map<String, Object>> findByDeploymentId(String deploymentId) {
        return jdbc.queryForList(sql.required("deploy-execution-find-by-id"), deploymentId).stream().findFirst();
    }

    public List<Map<String, Object>> instanceResults(String deploymentId) {
        return jdbc.queryForList(sql.required("deploy-execution-instance-results"), deploymentId);
    }

    @Transactional
    public void reconcileTerminal(
            String deploymentId,
            CommandState state,
            String stage,
            String message,
            String requestedBy,
            String approvedBy,
            String approvalRequestId,
            String reason) {
        int changed = jdbc.update(sql.required("deploy-execution-reconcile-terminal"),
                state.name(), stage, SensitiveTextSanitizer.sanitize(message),
                requestedBy, approvedBy, approvalRequestId, SensitiveTextSanitizer.sanitize(reason), deploymentId);
        if (changed != 1) throw new IllegalStateException("Deployment reconciliation terminal state was not persisted");
    }

    String requestHash(DeploymentRequest request) {
        try {
            byte[] canonical = canonicalJson.writeValueAsBytes(request);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonical));
        } catch (Exception failure) {
            throw new IllegalStateException("Deployment request cannot be canonicalized", failure);
        }
    }

    private static String scope(DeploymentRequest request) {
        String cellId = request.manifest().cellId();
        if (cellId == null || cellId.isBlank()) throw new IllegalArgumentException("Deployment cellId is required");
        return cellId;
    }

    private static void assertSameRequest(Map<String, Object> existing, String requestHash, DeploymentRequest request) {
        String existingHash = text(existing.get("request_hash"));
        String existingDeploymentId = text(existing.get("deployment_id"));
        String existingCellId = text(existing.get("cell_id"));
        if (existingHash.isBlank()) {
            throw new DeploymentIdempotencyConflictException(
                    "Legacy deployment execution has no canonical request hash and cannot be replayed safely");
        }
        if (!existingHash.equalsIgnoreCase(requestHash)
                || !existingDeploymentId.equals(request.deploymentId())
                || !existingCellId.equals(request.manifest().cellId())) {
            throw new DeploymentIdempotencyConflictException(
                    "Idempotency key was reused with a different deployment request");
        }
    }

    private static String text(Object value) { return value == null ? "" : value.toString().trim(); }
}
