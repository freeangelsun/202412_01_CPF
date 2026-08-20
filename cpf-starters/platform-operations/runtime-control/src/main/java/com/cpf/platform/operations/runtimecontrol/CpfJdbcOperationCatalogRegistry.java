package com.cpf.platform.operations.runtimecontrol;

import com.cpf.foundation.execution.api.CpfOperationCatalogRegistry;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runtime-discovered business online operations are synchronized to the canonical Operation Catalog.
 * Catalog metadata may be refreshed on every deployment, while ADM-owned policy is seeded only for a
 * newly discovered operation and is never overwritten by subsequent source scans.
 */
public final class CpfJdbcOperationCatalogRegistry implements CpfOperationCatalogRegistry {
    private static final String SYSTEM_TABLE = "OPS_SYSTEM_REGISTRY";
    private static final String CATALOG_TABLE = "OPS_OPERATION_CATALOG";
    private static final String POLICY_TABLE = "OPS_OPERATION_POLICY";
    private static final String CALLER_POLICY_TABLE = "OPS_OPERATION_CALLER_POLICY";
    private static final String DISCOVERY_TABLE = "OPS_OPERATION_DISCOVERY_INSTANCE";

    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final Clock clock;
    private final Set<String> defaultAllowedCallers;
    private final String seedSource;
    private final String seedRevision;

    public CpfJdbcOperationCatalogRegistry(
            JdbcTemplate jdbc,
            TransactionTemplate tx,
            Clock clock,
            List<String> defaultAllowedCallers,
            String seedSource,
            String seedRevision) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.tx = Objects.requireNonNull(tx, "tx");
        this.clock = Objects.requireNonNull(clock, "clock");
        LinkedHashSet<String> callers = new LinkedHashSet<>();
        if (defaultAllowedCallers != null) {
            for (String value : defaultAllowedCallers) {
                String caller = code(value);
                if (caller != null) callers.add(caller);
            }
        }
        this.defaultAllowedCallers = Set.copyOf(callers);
        this.seedSource = text(seedSource, "YML");
        this.seedRevision = text(seedRevision, "UNSPECIFIED");
    }

    @Override
    public SyncResult synchronize(SyncRequest request) {
        Objects.requireNonNull(request, "request");
        return tx.execute(status -> synchronizeInTransaction(request));
    }

    private SyncResult synchronizeInTransaction(SyncRequest request) {
        Timestamp now = Timestamp.from(clock.instant());
        String systemCode = requiredCode(request.systemCode(), "systemCode");
        String application = text(request.application(), null);
        ensureSystem(systemCode, request.domainCode(), request.instanceId(), now);

        Set<String> discovered = new LinkedHashSet<>();
        int inserted = 0;
        int updated = 0;
        int seeded = 0;
        for (Operation operation : request.operations()) {
            if (!systemCode.equals(requiredCode(operation.systemCode(), "operation.systemCode"))) {
                throw new IllegalArgumentException("Operation systemCode must match SyncRequest systemCode: " + operation.operationId());
            }
            String operationId = required(operation.operationId(), "operationId");
            if (!discovered.add(operationId)) {
                throw new IllegalArgumentException("Duplicate operationId in sync request: " + operationId);
            }
            boolean fresh = !operationExists(operationId);
            if (fresh) {
                insertOperation(operation, request.instanceId(), now);
                inserted++;
                seeded += seedPolicy(operationId, now);
            } else {
                updateOperation(operation, request.instanceId(), now);
                updated++;
            }
            upsertDiscovery(operationId, request, true, now);
        }

        // 현재 instance가 이번 artifact에서 발견하지 못한 Operation도 evidence로 남깁니다.
        // 단, 이 사실만으로 Catalog/ADM policy를 자동 비활성화하거나 삭제하지 않습니다.
        for (String existingOperationId : scopedOperationIds(systemCode, application)) {
            if (!discovered.contains(existingOperationId)) {
                upsertDiscovery(existingOperationId, request, false, now);
            }
        }

        // Rolling deployment에서는 구/신 Artifact가 동시에 살아 있을 수 있습니다.
        // 동일 application의 active lease 전체가 discovery report를 남긴 경우에만 Catalog discovery 상태를 집계합니다.
        // enabled/caller/system-domain/channel 정책은 이 경로에서 절대 변경하지 않습니다.
        updated += reconcileDiscoveryStatus(systemCode, application, now);
        return new SyncResult(request.operations().size(), inserted, updated, seeded);
    }

    private boolean operationExists(String operationId) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + CATALOG_TABLE + " WHERE operation_id=?",
                Integer.class,
                operationId);
        return count != null && count > 0;
    }

    private void insertOperation(Operation operation, String instanceId, Timestamp now) {
        jdbc.update(
                "INSERT INTO " + CATALOG_TABLE
                        + "(operation_id,operation_name,description,system_code,domain_code,application_code,http_method,api_path,"
                        + "controller_class,handler_method,openapi_operation_id,source_fingerprint,discovery_status,first_seen_at,last_seen_at,"
                        + "last_instance_id,metadata_version,created_by,created_at,updated_by,updated_at)"
                        + " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,1,'CPF_RUNTIME',?,'CPF_RUNTIME',?)",
                required(operation.operationId(), "operationId"),
                required(operation.name(), "name"),
                text(operation.description(), null),
                requiredCode(operation.systemCode(), "systemCode"),
                code(operation.domainCode()),
                text(operation.application(), null),
                text(operation.httpMethod(), "ANY").toUpperCase(Locale.ROOT),
                required(operation.apiPath(), "apiPath"),
                required(operation.controllerClass(), "controllerClass"),
                required(operation.handlerMethod(), "handlerMethod"),
                required(operation.operationId(), "operationId"),
                text(operation.sourceFingerprint(), null),
                "ACTIVE",
                now,
                now,
                text(instanceId, null),
                now,
                now);
    }

    private void updateOperation(Operation operation, String instanceId, Timestamp now) {
        jdbc.update(
                "UPDATE " + CATALOG_TABLE
                        + " SET operation_name=?,description=?,system_code=?,domain_code=?,application_code=?,http_method=?,api_path=?,"
                        + "controller_class=?,handler_method=?,openapi_operation_id=?,source_fingerprint=?,discovery_status='ACTIVE',last_seen_at=?,"
                        + "last_instance_id=?,metadata_version=metadata_version+1,updated_by='CPF_RUNTIME',updated_at=? WHERE operation_id=?",
                required(operation.name(), "name"),
                text(operation.description(), null),
                requiredCode(operation.systemCode(), "systemCode"),
                code(operation.domainCode()),
                text(operation.application(), null),
                text(operation.httpMethod(), "ANY").toUpperCase(Locale.ROOT),
                required(operation.apiPath(), "apiPath"),
                required(operation.controllerClass(), "controllerClass"),
                required(operation.handlerMethod(), "handlerMethod"),
                required(operation.operationId(), "operationId"),
                text(operation.sourceFingerprint(), null),
                now,
                text(instanceId, null),
                now,
                required(operation.operationId(), "operationId"));
    }


    private void upsertDiscovery(String operationId, SyncRequest request, boolean discovered, Timestamp now) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + DISCOVERY_TABLE + " WHERE operation_id=? AND instance_id=?",
                Integer.class, operationId, request.instanceId());
        String yn = discovered ? "Y" : "N";
        if (count == null || count == 0) {
            jdbc.update(
                    "INSERT INTO " + DISCOVERY_TABLE
                            + "(operation_id,instance_id,system_code,application_code,artifact_version,artifact_commit,discovered_yn,last_reported_at,"
                            + "created_by,created_at,updated_by,updated_at) VALUES (?,?,?,?,?,?,?,?,'CPF_RUNTIME',?,'CPF_RUNTIME',?)",
                    operationId, request.instanceId(), requiredCode(request.systemCode(), "systemCode"),
                    text(request.application(), null), text(request.artifactVersion(), null), text(request.artifactCommit(), null),
                    yn, now, now, now);
        } else {
            jdbc.update(
                    "UPDATE " + DISCOVERY_TABLE
                            + " SET system_code=?,application_code=?,artifact_version=?,artifact_commit=?,discovered_yn=?,last_reported_at=?,"
                            + "updated_by='CPF_RUNTIME',updated_at=? WHERE operation_id=? AND instance_id=?",
                    requiredCode(request.systemCode(), "systemCode"), text(request.application(), null),
                    text(request.artifactVersion(), null), text(request.artifactCommit(), null), yn, now, now,
                    operationId, request.instanceId());
        }
    }

    /**
     * Rolling deployment에서 한 instance의 scan만으로 NOT_DISCOVERED를 확정하지 않습니다.
     * Control Plane에서 현재 lease가 유효한 동일 application instance 전체가 discovery report를 남긴 경우에만 집계합니다.
     */
    private int reconcileDiscoveryStatus(String systemCode, String application, Timestamp now) {
        List<String> active = activeRuntimeInstances(application == null ? systemCode : application, now);
        if (active.isEmpty()) return 0; // Control Plane evidence가 없으면 false missing보다 기존 상태 보존을 우선합니다.
        int changed = 0;
        for (String operationId : scopedOperationIds(systemCode, application)) {
            Map<String,Boolean> reports = discoveryReports(operationId, active);
            if (reports.size() < active.size()) continue; // 아직 scan을 보고하지 않은 active instance가 있음.
            boolean present = reports.values().stream().anyMatch(Boolean::booleanValue);
            String target = present ? "ACTIVE" : "NOT_DISCOVERED";
            changed += jdbc.update(
                    "UPDATE " + CATALOG_TABLE
                            + " SET discovery_status=?, metadata_version=metadata_version+1, updated_by='CPF_RUNTIME', updated_at=?"
                            + " WHERE operation_id=? AND discovery_status<>?",
                    target, now, operationId, target);
        }
        return changed;
    }

    private List<String> scopedOperationIds(String systemCode, String application) {
        if (application == null || application.isBlank()) {
            return jdbc.query(
                    "SELECT operation_id FROM " + CATALOG_TABLE
                            + " WHERE system_code=? AND application_code IS NULL",
                    (rs, rowNum) -> rs.getString(1), systemCode);
        }
        return jdbc.query(
                "SELECT operation_id FROM " + CATALOG_TABLE
                        + " WHERE system_code=? AND application_code=?",
                (rs, rowNum) -> rs.getString(1), systemCode, application);
    }

    private List<String> activeRuntimeInstances(String serviceId, Timestamp now) {
        try {
            return jdbc.query(
                    "SELECT s.instance_id FROM OPS_RUNTIME_INSTANCE_STATE s JOIN OPS_SERVICE_INSTANCE i ON i.instance_id=s.instance_id"
                            + " WHERE s.lease_until>? AND i.active_yn='Y' AND i.service_id=?",
                    (rs, rowNum) -> rs.getString(1), now, serviceId);
        } catch (DataAccessException unavailable) {
            return List.of();
        }
    }

    private Map<String,Boolean> discoveryReports(String operationId, List<String> activeInstances) {
        if (activeInstances.isEmpty()) return Map.of();
        String placeholders = String.join(",", java.util.Collections.nCopies(activeInstances.size(), "?"));
        ArrayList<Object> args = new ArrayList<>();
        args.add(operationId);
        args.addAll(activeInstances);
        Map<String,Boolean> result = new java.util.LinkedHashMap<>();
        jdbc.query("SELECT instance_id,discovered_yn FROM " + DISCOVERY_TABLE
                        + " WHERE operation_id=? AND instance_id IN (" + placeholders + ")",
                (RowCallbackHandler) rs -> result.put(rs.getString(1), "Y".equalsIgnoreCase(rs.getString(2))), args.toArray());
        return Map.copyOf(result);
    }

    private void ensureSystem(String systemCode, String domainCode, String instanceId, Timestamp now) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM " + SYSTEM_TABLE + " WHERE system_code=?",
                Integer.class,
                systemCode);
        if (count == null || count == 0) {
            jdbc.update(
                    "INSERT INTO " + SYSTEM_TABLE
                            + "(system_code,system_name,domain_code,enabled_yn,description,policy_version,first_seen_at,last_seen_at,last_instance_id,"
                            + "created_by,created_at,updated_by,updated_at) VALUES (?,?,?,'Y',?,1,?,?,?,'CPF_RUNTIME',?,'CPF_RUNTIME',?)",
                    systemCode,
                    systemCode,
                    code(domainCode),
                    "Runtime-discovered system",
                    now,
                    now,
                    text(instanceId, null),
                    now,
                    now);
        } else {
            jdbc.update(
                    "UPDATE " + SYSTEM_TABLE
                            + " SET domain_code=?,last_seen_at=?,last_instance_id=?,updated_by='CPF_RUNTIME',updated_at=? WHERE system_code=?",
                    code(domainCode), now, text(instanceId, null), now, systemCode);
        }
    }

    private int seedPolicy(String operationId, Timestamp now) {
        boolean all = defaultAllowedCallers.contains("ALL");
        jdbc.update(
                "INSERT INTO " + POLICY_TABLE
                        + "(operation_id,enabled_yn,all_callers_yn,channel_policy_required_yn,policy_version,seed_source,seed_revision,seeded_at,"
                        + "change_reason,created_by,created_at,updated_by,updated_at) VALUES (?,'Y',?,'N',1,?,?,?,?, 'CPF_SEED',?,'CPF_SEED',?)",
                operationId,
                all ? "Y" : "N",
                seedSource,
                seedRevision,
                now,
                "Initial operation policy seed",
                now,
                now);
        int rows = 1;
        if (!all) {
            for (String caller : defaultAllowedCallers) {
                jdbc.update(
                        "INSERT INTO " + CALLER_POLICY_TABLE
                                + "(operation_id,caller_system_code,allowed_yn,policy_version,seed_source,seed_revision,seeded_at,change_reason,"
                                + "created_by,created_at,updated_by,updated_at) VALUES (?,?,'Y',1,?,?,?,?, 'CPF_SEED',?,'CPF_SEED',?)",
                        operationId,
                        caller,
                        seedSource,
                        seedRevision,
                        now,
                        "Initial operation caller seed",
                        now,
                        now);
                rows++;
            }
        }
        return rows;
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }

    private static String requiredCode(String value, String field) {
        String code = code(value);
        if (code == null) throw new IllegalArgumentException(field + " is required");
        return code;
    }

    private static String code(String value) {
        return value == null || value.isBlank() ? null : value.trim().toUpperCase(Locale.ROOT);
    }

    private static String text(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim();
    }
}
