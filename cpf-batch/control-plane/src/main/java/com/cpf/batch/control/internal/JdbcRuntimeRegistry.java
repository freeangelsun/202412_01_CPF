package com.cpf.batch.control.internal;

import com.cpf.batch.api.DesiredState;
import com.cpf.batch.api.RuntimeHeartbeat;
import com.cpf.batch.api.RuntimeRegistration;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.platform.operations.runtimecontrol.api.CpfManagedRuntimeRegistry;
import com.cpf.platform.operations.runtimecontrol.api.CpfManagedRuntimeSnapshot;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Batch Runtime의 중앙 Runtime Registry adapter입니다.
 *
 * <p>등록/lease/fencing/desired/actual lifecycle의 master authority는 {@link CpfManagedRuntimeRegistry}입니다.
 * Batch DB에는 capability와 capacity/execution heartbeat event만 projection/telemetry로 저장합니다.</p>
 */
@Repository
public class JdbcRuntimeRegistry {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;
    private final CpfManagedRuntimeRegistry central;

    public JdbcRuntimeRegistry(
            JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider sqlCatalogProvider,
            CpfManagedRuntimeRegistry central) {
        this.jdbc = jdbc;
        this.sql = sqlCatalogProvider.forModule("bat");
        this.central = central;
    }

    @Transactional
    public void register(RuntimeRegistration registration) {
        // CPF 공통 Runtime Control Agent가 먼저 중앙 authoritative registration을 소유합니다.
        CpfManagedRuntimeSnapshot centralState = central.snapshot(registration.instanceId());
        if (centralState.serviceId() != null && !centralState.serviceId().equals(registration.serviceId())) {
            throw new IllegalStateException(
                    "Batch Runtime serviceId가 중앙 Registry와 다릅니다: instance=" + registration.instanceId());
        }
        jdbc.update(sql.required("runtime-capabilities-delete"), registration.instanceId());
        for (String capability : registration.capabilities()) {
            jdbc.update(sql.required("runtime-capability-insert"), registration.instanceId(), capability);
        }
    }

    @Transactional
    public void heartbeat(RuntimeHeartbeat heartbeat) {
        // actual lifecycle도 중앙 state에 기록하고, Batch DB에는 capacity/execution telemetry만 남깁니다.
        central.reportActualState(heartbeat.instanceId(), heartbeat.actualState().name());
        jdbc.update(sql.required("runtime-heartbeat-insert"),
                heartbeat.instanceId(), Timestamp.from(heartbeat.timestamp()), heartbeat.ready() ? "Y" : "N",
                heartbeat.availableCapacity(), heartbeat.queueDepth(), heartbeat.draining() ? "Y" : "N",
                heartbeat.currentExecutions().size(), heartbeat.activeLeases().size(), heartbeat.lastErrorCode(),
                heartbeat.deploymentVersion());
    }

    @Transactional
    public long updateDesiredState(String instanceId, DesiredState desired, long expectedVersion) {
        if (desired == null) throw new IllegalArgumentException("desiredState is required");
        return central.updateDesiredState(instanceId, desired.name(), expectedVersion);
    }

    public Map<String,Object> snapshot(String instanceId) {
        return asMap(central.snapshot(instanceId));
    }

    public List<Map<String,Object>> list(Duration stale) {
        return central.list(stale).stream().map(JdbcRuntimeRegistry::asMap).toList();
    }

    private static Map<String,Object> asMap(CpfManagedRuntimeSnapshot state) {
        LinkedHashMap<String,Object> row = new LinkedHashMap<>();
        row.put("instance_id", state.instanceId());
        row.put("service_id", state.serviceId());
        row.put("runtime_role", state.runtimeRole());
        row.put("desired_state", state.desiredState());
        row.put("actual_state", state.actualState());
        row.put("row_version", state.controlVersion());
        row.put("fencing_token", state.fencingToken());
        row.put("lease_until", state.leaseUntil());
        row.put("last_heartbeat_at", state.lastHeartbeatAt());
        row.put("environment_code", state.environment());
        row.put("zone_id", state.zone());
        row.put("cell_id", state.cell());
        row.put("artifact_version", state.artifactVersion());
        return Map.copyOf(row);
    }
}
