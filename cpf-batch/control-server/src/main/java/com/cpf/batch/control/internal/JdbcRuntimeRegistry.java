package com.cpf.batch.control.internal;

import com.cpf.batch.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;

@Repository
public class JdbcRuntimeRegistry {
    private final JdbcTemplate jdbc; public JdbcRuntimeRegistry(JdbcTemplate jdbc){this.jdbc=jdbc;}

    @Transactional
    public void register(RuntimeRegistration r) {
        int n=jdbc.update("""
          UPDATE bat_runtime_instance SET runtime_role=?,service_id=?,was_id=?,host_alias=?,zone_id=?,pool_id=?,
            artifact_version=?,git_sha=?,artifact_checksum=?,profile_name=?,config_version=?,schema_compatibility=?,
            started_at=?,actual_state='STARTING',updated_at=CURRENT_TIMESTAMP(6) WHERE instance_id=?
          """,r.runtimeRole().name(),r.serviceId(),r.wasId(),r.hostAlias(),r.zone(),r.pool(),r.artifactVersion(),r.gitSha(),r.checksum(),
          r.profile(),r.configVersion(),r.schemaCompatibility(),Timestamp.from(r.startedAt()),r.instanceId());
        if(n==0) jdbc.update("""
          INSERT INTO bat_runtime_instance(instance_id,runtime_role,service_id,was_id,host_alias,zone_id,pool_id,
            artifact_version,git_sha,artifact_checksum,profile_name,desired_state,actual_state,config_version,schema_compatibility,started_at,row_version)
          VALUES(?,?,?,?,?,?,?,?,?,?,?,'RUNNING','STARTING',?,?,?,0)
          """,r.instanceId(),r.runtimeRole().name(),r.serviceId(),r.wasId(),r.hostAlias(),r.zone(),r.pool(),r.artifactVersion(),r.gitSha(),r.checksum(),r.profile(),r.configVersion(),r.schemaCompatibility(),Timestamp.from(r.startedAt()));
        jdbc.update("DELETE FROM bat_runtime_capability WHERE instance_id=?",r.instanceId());
        for(String c:r.capabilities()) jdbc.update("INSERT INTO bat_runtime_capability(instance_id,capability_code) VALUES(?,?)",r.instanceId(),c);
    }

    @Transactional
    public void heartbeat(RuntimeHeartbeat h) {
        if(jdbc.update("""
          UPDATE bat_runtime_instance SET actual_state=?,last_heartbeat_at=?,fencing_token=?,row_version=row_version+1,updated_at=CURRENT_TIMESTAMP(6)
           WHERE instance_id=?
          """,h.actualState().name(),Timestamp.from(h.timestamp()),h.fencingToken(),h.instanceId())!=1)
            throw new IllegalStateException("Unregistered runtime: "+h.instanceId());
        jdbc.update("""
          INSERT INTO bat_runtime_heartbeat(instance_id,heartbeat_at,ready_yn,available_capacity,queue_depth,draining_yn,
            current_execution_count,active_lease_count,last_error_code,deployment_version)
          VALUES(?,?,?,?,?,?,?,?,?,?)
          """,h.instanceId(),Timestamp.from(h.timestamp()),h.ready()?"Y":"N",h.availableCapacity(),h.queueDepth(),h.draining()?"Y":"N",
          h.currentExecutions().size(),h.activeLeases().size(),h.lastErrorCode(),h.deploymentVersion());
    }

    @Transactional
    public long updateDesiredState(String instanceId,DesiredState desired,long expectedVersion) {
        Long current=jdbc.queryForObject("SELECT row_version FROM bat_runtime_instance WHERE instance_id=?",Long.class,instanceId);
        if(current==null) throw new IllegalArgumentException("Runtime instance not found: "+instanceId);
        long version=expectedVersion>0?expectedVersion:current;
        int changed=jdbc.update("""
          UPDATE bat_runtime_instance SET desired_state=?,row_version=row_version+1,updated_at=CURRENT_TIMESTAMP(6)
           WHERE instance_id=? AND row_version=?
          """,desired.name(),instanceId,version);
        if(changed!=1) throw new IllegalStateException("Runtime state changed concurrently: "+instanceId);
        return version+1;
    }

    public Map<String,Object> snapshot(String instanceId) {
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT * FROM bat_runtime_instance WHERE instance_id=?",instanceId);
        if(rows.isEmpty()) throw new IllegalArgumentException("Runtime instance not found: "+instanceId);
        return rows.get(0);
    }

    public List<Map<String,Object>> list(Duration stale) {
        return jdbc.queryForList("""
          SELECT i.instance_id,i.runtime_role,i.service_id,i.was_id,i.host_alias,i.zone_id,i.pool_id,i.artifact_version,
            i.git_sha,i.profile_name,i.desired_state,
            CASE WHEN i.actual_state='FAILED' THEN 'FAILED' WHEN i.last_heartbeat_at IS NULL THEN 'UNKNOWN'
                 WHEN i.last_heartbeat_at<? THEN 'STALE' ELSE i.actual_state END effective_state,
            i.last_heartbeat_at,i.started_at,i.fencing_token,i.row_version,
            (SELECT GROUP_CONCAT(c.capability_code ORDER BY c.capability_code) FROM bat_runtime_capability c WHERE c.instance_id=i.instance_id) capabilities
          FROM bat_runtime_instance i ORDER BY i.runtime_role,i.instance_id
          """,Timestamp.from(Instant.now().minus(stale)));
    }
}
