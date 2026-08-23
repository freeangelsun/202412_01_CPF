package com.cpf.platform.operations.runtimecontrol;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.foundation.execution.api.CpfOperationCatalogRegistry;
import java.sql.Timestamp;
import java.time.Clock;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Runtime-discovered business online operations are synchronized to the canonical Operation Catalog.
 * Catalog metadata may be refreshed on every deployment, while ADM-owned policy is seeded only for a
 * newly discovered operation and is never overwritten by subsequent source scans.
 */
public final class CpfJdbcOperationCatalogRegistry implements CpfOperationCatalogRegistry {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final CpfVendorSqlCatalog sql;
    private final Clock clock;
    private final Set<String> defaultAllowedCallers;
    private final String seedSource;
    private final String seedRevision;

    public CpfJdbcOperationCatalogRegistry(
            JdbcTemplate jdbc,
            TransactionTemplate tx,
            CpfVendorSqlCatalogProvider catalogs,
            Clock clock,
            List<String> defaultAllowedCallers,
            String seedSource,
            String seedRevision) {
        this.jdbc = Objects.requireNonNull(jdbc, "jdbc");
        this.tx = Objects.requireNonNull(tx, "tx");
        this.sql = Objects.requireNonNull(catalogs, "catalogs").forModule("cpf");
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
                sql.required("operation-catalog-exists"),
                Integer.class,
                operationId);
        return count != null && count > 0;
    }

    private void insertOperation(Operation operation, String instanceId, Timestamp now) {
        jdbc.update(
                sql.required("operation-catalog-insert"),
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
                sql.required("operation-catalog-update"),
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
                sql.required("operation-discovery-exists"),
                Integer.class, operationId, request.instanceId());
        String yn = discovered ? "Y" : "N";
        if (count == null || count == 0) {
            jdbc.update(
                    sql.required("operation-discovery-insert"),
                    operationId, request.instanceId(), requiredCode(request.systemCode(), "systemCode"),
                    text(request.application(), null), text(request.artifactVersion(), null), text(request.artifactCommit(), null),
                    yn, now, now, now);
        } else {
            jdbc.update(
                    sql.required("operation-discovery-update"),
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
                    sql.required("operation-catalog-update-discovery-status"),
                    target, now, operationId, target);
        }
        return changed;
    }

    private List<String> scopedOperationIds(String systemCode, String application) {
        if (application == null || application.isBlank()) {
            return jdbc.query(
                    sql.required("operation-catalog-find-scoped-null-application"),
                    (rs, rowNum) -> rs.getString(1), systemCode);
        }
        return jdbc.query(
                sql.required("operation-catalog-find-scoped-application"),
                (rs, rowNum) -> rs.getString(1), systemCode, application);
    }

    private List<String> activeRuntimeInstances(String serviceId, Timestamp now) {
        try {
            return jdbc.query(
                    sql.required("operation-runtime-active-instance-find-by-service"),
                    (rs, rowNum) -> rs.getString(1), now, serviceId);
        } catch (DataAccessException unavailable) {
            return List.of();
        }
    }

    private Map<String,Boolean> discoveryReports(String operationId, List<String> activeInstances) {
        if (activeInstances.isEmpty()) return Map.of();
        Set<String> active = Set.copyOf(activeInstances);
        Map<String,Boolean> result = new java.util.LinkedHashMap<>();
        jdbc.query(sql.required("operation-discovery-find-by-operation"), rs -> {
            String instanceId = rs.getString(1);
            if (active.contains(instanceId)) {
                result.put(instanceId, "Y".equalsIgnoreCase(rs.getString(2)));
            }
        }, operationId);
        return Map.copyOf(result);
    }

    private void ensureSystem(String systemCode, String domainCode, String instanceId, Timestamp now) {
        Integer count = jdbc.queryForObject(
                sql.required("operation-system-registry-exists"),
                Integer.class,
                systemCode);
        if (count == null || count == 0) {
            jdbc.update(
                    sql.required("operation-system-registry-insert"),
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
                    sql.required("operation-system-registry-update"),
                    code(domainCode), now, text(instanceId, null), now, systemCode);
        }
    }

    private int seedPolicy(String operationId, Timestamp now) {
        boolean all = defaultAllowedCallers.contains("ALL");
        jdbc.update(
                sql.required("operation-policy-insert-seed"),
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
                        sql.required("operation-caller-policy-insert-seed"),
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
