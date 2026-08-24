package com.cpf.batch.control.retention;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** DB3-portable retention policy/run/lease repository. Time comparisons use caller-provided UTC instants. */
@Repository
public class BatRetentionExecutionRepository {
    private final JdbcTemplate jdbc;
    private final CpfVendorSqlCatalog sql;
    public BatRetentionExecutionRepository(
            @Qualifier("batJdbcTemplate") JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc = jdbc;
        this.sql = sqlCatalogProvider.forModule("bat");
    }

    public List<String> findDuePolicyIds(Instant now, int limit) {
        return jdbc.query(con -> {
            var ps = con.prepareStatement(sql.required("retention-policy-find-due"));
            ps.setTimestamp(1, Timestamp.from(now)); ps.setMaxRows(limit); return ps;
        }, (rs, n) -> rs.getString(1));
    }

    public List<BatRetentionPolicyDefinition> findPolicies() {
        return jdbc.query(sql.required("retention-policy-list"), (rs,n) -> policy(rs));
    }
    public Optional<BatRetentionPolicyDefinition> findPolicy(String id) {
        List<BatRetentionPolicyDefinition> rows=jdbc.query(sql.required("retention-policy-find"), (rs,n)->policy(rs), id);
        return rows.stream().findFirst();
    }

    public BatRetentionPolicyDefinition savePolicy(BatRetentionPolicyDefinition p, String actor) {
        int updated=jdbc.update(sql.required("retention-policy-update"),
                p.target(),p.action(),p.retentionDays(),p.scheduleExpression(),time(p.maintenanceStart()),time(p.maintenanceEnd()),yn(p.enabled()),yn(p.legalHold()),p.chunkSize(),p.throttleMillis(),p.maxRowsPerRun(),p.maxRuntimeSeconds(),p.leaseSeconds(),p.policyVersion(),ts(p.nextRunAt()),actor,p.policyId(),p.rowVersion());
        if(updated==0) {
            if(findPolicy(p.policyId()).isPresent()) throw new IllegalStateException("RETENTION_POLICY_VERSION_CONFLICT");
            jdbc.update(sql.required("retention-policy-insert"),
                    p.policyId(),p.target(),p.action(),p.retentionDays(),p.scheduleExpression(),time(p.maintenanceStart()),time(p.maintenanceEnd()),yn(p.enabled()),yn(p.legalHold()),p.chunkSize(),p.throttleMillis(),p.maxRowsPerRun(),p.maxRuntimeSeconds(),p.leaseSeconds(),p.policyVersion(),ts(p.nextRunAt()),actor,actor);
        }
        return findPolicy(p.policyId()).orElseThrow();
    }

    public boolean claim(String policyId,String owner,Instant now,Instant until) {
        return claim(policyId, owner, now, until, null);
    }
    public boolean claim(String policyId,String owner,Instant now,Instant until,Long expectedVersion) {
        if(expectedVersion==null) {
            return jdbc.update(sql.required("retention-policy-claim"),
                    owner,Timestamp.from(until),policyId,Timestamp.from(now),owner)==1;
        }
        return jdbc.update(sql.required("retention-policy-claim-version"),
                owner,Timestamp.from(until),policyId,expectedVersion,Timestamp.from(now),owner)==1;
    }
    /** Extend the current owner's lease only while the lease is still valid. Losing the lease is fail-close. */
    public boolean renewLease(String policyId,String owner,Instant now,Instant until) {
        return jdbc.update(sql.required("retention-policy-renew"),
                Timestamp.from(until),policyId,owner,Timestamp.from(now))==1;
    }
    public void release(String policyId,String owner,Instant nextRunAt) {
        jdbc.update(sql.required("retention-policy-release"), ts(nextRunAt),policyId,owner);
    }
    public void setPolicyPaused(String policyId, boolean paused, String actor, long expectedVersion) {
        int updated=jdbc.update(sql.required("retention-policy-pause"),
                yn(paused),actor,policyId,expectedVersion);
        if(updated!=1) throw new IllegalStateException("RETENTION_POLICY_VERSION_CONFLICT");
    }

    public void createRun(BatRetentionRunSnapshot r) {
        jdbc.update(sql.required("retention-run-insert"),
                r.runId(),r.policyId(),r.triggerType(),r.status(),r.runtimeInstanceId(),r.actorId(),r.reason(),r.policyVersion(),ts(r.cutoffAt()),ts(r.startedAt()),r.matchedCount(),r.archivedCount(),r.deletedCount(),r.processedCount(),r.compressedCount(),r.freedBytes(),yn(r.pauseRequested()));
    }
    public Optional<BatRetentionRunSnapshot> findRun(String runId) {
        List<BatRetentionRunSnapshot> rows=jdbc.query(sql.required("retention-run-find"),(rs,n)->run(rs),runId);
        return rows.stream().findFirst();
    }
    public List<BatRetentionRunSnapshot> findRuns(String policyId,int limit) {
        if(policyId==null || policyId.isBlank()) {
            return jdbc.query(con->{var ps=con.prepareStatement(sql.required("retention-run-list"));ps.setMaxRows(limit);return ps;},(rs,n)->run(rs));
        }
        return jdbc.query(con->{var ps=con.prepareStatement(sql.required("retention-run-list-policy"));ps.setString(1,policyId);ps.setMaxRows(limit);return ps;},(rs,n)->run(rs));
    }
    public void requestPause(String runId,String actor,String reason,long expectedVersion) {
        if (expectedVersion < 0) throw new IllegalArgumentException("expectedVersion은 0 이상이어야 합니다.");
        int updated=jdbc.update(
                sql.required("retention-run-request-pause"),
                actor,safe(reason),runId,expectedVersion);
        if(updated!=1) throw new IllegalStateException("RETENTION_RUN_STATE_OR_POLICY_VERSION_CONFLICT");
    }
    public boolean pauseRequested(String runId) {
        Boolean v=jdbc.queryForObject(sql.required("retention-run-pause-requested"),(rs,n)->rs.getInt(1)==1,runId);
        return Boolean.TRUE.equals(v);
    }
    public void markRunning(String runId,String runtime,String actor) {
        jdbc.update(sql.required("retention-run-mark-running"),runtime,actor,actor,runId);
    }
    public void progress(String runId,long matched,long archived,long deleted,long processed,long compressed,long freed) {
        jdbc.update(sql.required("retention-run-progress"),matched,archived,deleted,processed,compressed,freed,runId);
    }
    public void finish(String runId,String status,String errorCode,String summary) {
        jdbc.update(sql.required("retention-run-finish"),status,errorCode,safe(summary),runId);
    }

    public void audit(String operation,String targetType,String targetId,String requestedBy,String approvedBy,
                      String approvalRequestId,String reason,Long expectedVersion,String resultState) {
        jdbc.update(sql.required("retention-audit-insert"),
                java.util.UUID.randomUUID().toString(),operation,targetType,targetId,requestedBy,approvedBy,approvalRequestId,
                safe(reason),expectedVersion,resultState);
    }

    public List<Map<String,Object>> findAuditsByApprovalRequestId(String approvalRequestId) {
        if (approvalRequestId == null || approvalRequestId.isBlank()) return List.of();
        return jdbc.queryForList(sql.required("retention-audit-find-approval"), approvalRequestId.trim());
    }

    private static BatRetentionPolicyDefinition policy(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new BatRetentionPolicyDefinition(rs.getString("policy_id"),rs.getString("target_name"),rs.getString("action_name"),rs.getInt("retention_days"),rs.getString("schedule_expression"),lt(rs.getString("maintenance_start")),lt(rs.getString("maintenance_end")),"Y".equals(rs.getString("enabled_yn")),"Y".equals(rs.getString("legal_hold_yn")),rs.getInt("chunk_size"),rs.getLong("throttle_millis"),rs.getLong("max_rows_per_run"),rs.getLong("max_runtime_seconds"),rs.getInt("lease_seconds"),rs.getLong("policy_version"),instant(rs.getTimestamp("next_run_at")),rs.getLong("row_version"));
    }
    private static BatRetentionRunSnapshot run(java.sql.ResultSet rs)throws java.sql.SQLException {return new BatRetentionRunSnapshot(rs.getString("run_id"),rs.getString("policy_id"),rs.getString("trigger_type"),rs.getString("status"),rs.getString("runtime_instance_id"),rs.getString("actor_id"),rs.getString("reason"),rs.getLong("policy_version"),instant(rs.getTimestamp("cutoff_at")),instant(rs.getTimestamp("started_at")),instant(rs.getTimestamp("completed_at")),rs.getLong("matched_count"),rs.getLong("archived_count"),rs.getLong("deleted_count"),rs.getLong("processed_count"),rs.getLong("compressed_count"),rs.getLong("freed_bytes"),"Y".equals(rs.getString("pause_requested_yn")),rs.getString("error_code"),rs.getString("error_summary"));}
    private static String yn(boolean b){return b?"Y":"N";} private static Timestamp ts(Instant v){return v==null?null:Timestamp.from(v);} private static Instant instant(Timestamp v){return v==null?null:v.toInstant();} private static String time(LocalTime v){return v==null?null:v.toString();} private static LocalTime lt(String v){return v==null||v.isBlank()?null:LocalTime.parse(v);} private static String safe(String s){return s==null?null:s.substring(0,Math.min(500,s.length()));}
}
