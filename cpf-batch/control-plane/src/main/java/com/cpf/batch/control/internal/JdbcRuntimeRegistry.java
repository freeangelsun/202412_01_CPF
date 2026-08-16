package com.cpf.batch.control.internal;

import com.cpf.batch.api.*;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;

@Repository
public class JdbcRuntimeRegistry {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;

    public JdbcRuntimeRegistry(JdbcTemplate jdbc, CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = jdbc;
        this.sql = sqlCatalogProvider.forModule("bat");
    }

    @Transactional
    public void register(RuntimeRegistration r) {
        int n=jdbc.update(sql.required("runtime-registry-update"),
          r.runtimeRole().name(),r.serviceId(),r.wasId(),r.hostAlias(),r.zone(),r.pool(),r.artifactVersion(),r.gitSha(),r.checksum(),
          r.profile(),r.configVersion(),r.schemaCompatibility(),Timestamp.from(r.startedAt()),r.instanceId());
        if(n==0) jdbc.update(sql.required("runtime-registry-insert"),
                r.instanceId(),r.runtimeRole().name(),r.serviceId(),r.wasId(),r.hostAlias(),r.zone(),r.pool(),r.artifactVersion(),r.gitSha(),r.checksum(),r.profile(),r.configVersion(),r.schemaCompatibility(),Timestamp.from(r.startedAt()));
        jdbc.update(sql.required("runtime-capabilities-delete"),r.instanceId());
        for(String c:r.capabilities()) jdbc.update(sql.required("runtime-capability-insert"),r.instanceId(),c);
    }

    @Transactional
    public void heartbeat(RuntimeHeartbeat h) {
        if(jdbc.update(sql.required("runtime-heartbeat-update"),
                h.actualState().name(),Timestamp.from(h.timestamp()),h.fencingToken(),h.instanceId())!=1)
            throw new IllegalStateException("Unregistered runtime: "+h.instanceId());
        jdbc.update(sql.required("runtime-heartbeat-insert"),
          h.instanceId(),Timestamp.from(h.timestamp()),h.ready()?"Y":"N",h.availableCapacity(),h.queueDepth(),h.draining()?"Y":"N",
          h.currentExecutions().size(),h.activeLeases().size(),h.lastErrorCode(),h.deploymentVersion());
    }

    @Transactional
    public long updateDesiredState(String instanceId,DesiredState desired,long expectedVersion) {
        Long current=jdbc.queryForObject(sql.required("runtime-row-version"),Long.class,instanceId);
        if(current==null) throw new IllegalArgumentException("Runtime instance not found: "+instanceId);
        long version=expectedVersion>0?expectedVersion:current;
        int changed=jdbc.update(
                sql.required("runtime-desired-state-update"),desired.name(),instanceId,version);
        if(changed!=1) throw new IllegalStateException("Runtime state changed concurrently: "+instanceId);
        return version+1;
    }

    public Map<String,Object> snapshot(String instanceId) {
        List<Map<String,Object>> rows=
                jdbc.queryForList(sql.required("runtime-instance-snapshot"),instanceId);
        if(rows.isEmpty()) throw new IllegalArgumentException("Runtime instance not found: "+instanceId);
        return rows.get(0);
    }

    public List<Map<String,Object>> list(Duration stale) {
        return jdbc.queryForList(
                sql.required("runtime-instances-list"),
                Timestamp.from(Instant.now().minus(stale)));
    }
}
