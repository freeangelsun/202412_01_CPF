package com.cpf.batch.control.retention;

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
    public BatRetentionExecutionRepository(@Qualifier("batJdbcTemplate") JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public List<String> findDuePolicyIds(Instant now, int limit) {
        return jdbc.query(con -> {
            var ps = con.prepareStatement("SELECT policy_id FROM ops_retention_policy WHERE enabled_yn='Y' AND paused_yn='N' AND next_run_at IS NOT NULL AND next_run_at<=? ORDER BY next_run_at, policy_id");
            ps.setTimestamp(1, Timestamp.from(now)); ps.setMaxRows(limit); return ps;
        }, (rs, n) -> rs.getString(1));
    }

    public List<BatRetentionPolicyDefinition> findPolicies() {
        return jdbc.query("SELECT * FROM ops_retention_policy ORDER BY policy_id", (rs,n) -> policy(rs));
    }
    public Optional<BatRetentionPolicyDefinition> findPolicy(String id) {
        List<BatRetentionPolicyDefinition> rows=jdbc.query("SELECT * FROM ops_retention_policy WHERE policy_id=?", (rs,n)->policy(rs), id);
        return rows.stream().findFirst();
    }

    public BatRetentionPolicyDefinition savePolicy(BatRetentionPolicyDefinition p, String actor) {
        int updated=jdbc.update("UPDATE ops_retention_policy SET target_name=?,action_name=?,retention_days=?,schedule_expression=?,maintenance_start=?,maintenance_end=?,enabled_yn=?,legal_hold_yn=?,chunk_size=?,throttle_millis=?,max_rows_per_run=?,max_runtime_seconds=?,lease_seconds=?,policy_version=?,next_run_at=?,row_version=row_version+1,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE policy_id=? AND row_version=?",
                p.target(),p.action(),p.retentionDays(),p.scheduleExpression(),time(p.maintenanceStart()),time(p.maintenanceEnd()),yn(p.enabled()),yn(p.legalHold()),p.chunkSize(),p.throttleMillis(),p.maxRowsPerRun(),p.maxRuntimeSeconds(),p.leaseSeconds(),p.policyVersion(),ts(p.nextRunAt()),actor,p.policyId(),p.rowVersion());
        if(updated==0) {
            if(findPolicy(p.policyId()).isPresent()) throw new IllegalStateException("RETENTION_POLICY_VERSION_CONFLICT");
            jdbc.update("INSERT INTO ops_retention_policy(policy_id,target_name,action_name,retention_days,schedule_expression,maintenance_start,maintenance_end,enabled_yn,paused_yn,legal_hold_yn,chunk_size,throttle_millis,max_rows_per_run,max_runtime_seconds,lease_seconds,policy_version,next_run_at,fencing_token,row_version,created_by,updated_by) VALUES(?,?,?,?,?,?,?,?, 'N',?,?,?,?,?,?,?,?,0,0,?,?)",
                    p.policyId(),p.target(),p.action(),p.retentionDays(),p.scheduleExpression(),time(p.maintenanceStart()),time(p.maintenanceEnd()),yn(p.enabled()),yn(p.legalHold()),p.chunkSize(),p.throttleMillis(),p.maxRowsPerRun(),p.maxRuntimeSeconds(),p.leaseSeconds(),p.policyVersion(),ts(p.nextRunAt()),actor,actor);
        }
        return findPolicy(p.policyId()).orElseThrow();
    }

    public boolean claim(String policyId,String owner,Instant now,Instant until) {
        return claim(policyId, owner, now, until, null);
    }
    public boolean claim(String policyId,String owner,Instant now,Instant until,Long expectedVersion) {
        if(expectedVersion==null) {
            return jdbc.update("UPDATE ops_retention_policy SET lease_owner=?,lease_until=?,fencing_token=fencing_token+1,updated_at=CURRENT_TIMESTAMP WHERE policy_id=? AND enabled_yn='Y' AND paused_yn='N' AND (lease_until IS NULL OR lease_until<? OR lease_owner=?)",
                    owner,Timestamp.from(until),policyId,Timestamp.from(now),owner)==1;
        }
        return jdbc.update("UPDATE ops_retention_policy SET lease_owner=?,lease_until=?,fencing_token=fencing_token+1,updated_at=CURRENT_TIMESTAMP WHERE policy_id=? AND row_version=? AND enabled_yn='Y' AND paused_yn='N' AND (lease_until IS NULL OR lease_until<? OR lease_owner=?)",
                owner,Timestamp.from(until),policyId,expectedVersion,Timestamp.from(now),owner)==1;
    }
    /** Extend the current owner's lease only while the lease is still valid. Losing the lease is fail-close. */
    public boolean renewLease(String policyId,String owner,Instant now,Instant until) {
        return jdbc.update("UPDATE ops_retention_policy SET lease_until=?,updated_at=CURRENT_TIMESTAMP WHERE policy_id=? AND lease_owner=? AND lease_until>=?",
                Timestamp.from(until),policyId,owner,Timestamp.from(now))==1;
    }
    public void release(String policyId,String owner,Instant nextRunAt) {
        jdbc.update("UPDATE ops_retention_policy SET lease_owner=NULL,lease_until=NULL,last_run_at=CURRENT_TIMESTAMP,next_run_at=?,updated_at=CURRENT_TIMESTAMP WHERE policy_id=? AND lease_owner=?", ts(nextRunAt),policyId,owner);
    }
    public void setPolicyPaused(String policyId, boolean paused, String actor, long expectedVersion) {
        int updated=jdbc.update("UPDATE ops_retention_policy SET paused_yn=?,row_version=row_version+1,updated_by=?,updated_at=CURRENT_TIMESTAMP WHERE policy_id=? AND row_version=?",
                yn(paused),actor,policyId,expectedVersion);
        if(updated!=1) throw new IllegalStateException("RETENTION_POLICY_VERSION_CONFLICT");
    }

    public void createRun(BatRetentionRunSnapshot r) {
        jdbc.update("INSERT INTO ops_retention_run(run_id,policy_id,trigger_type,status,runtime_instance_id,actor_id,reason,policy_version,cutoff_at,started_at,matched_count,archived_count,deleted_count,processed_count,compressed_count,freed_bytes,pause_requested_yn,created_at,updated_at) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,CURRENT_TIMESTAMP)",
                r.runId(),r.policyId(),r.triggerType(),r.status(),r.runtimeInstanceId(),r.actorId(),r.reason(),r.policyVersion(),ts(r.cutoffAt()),ts(r.startedAt()),r.matchedCount(),r.archivedCount(),r.deletedCount(),r.processedCount(),r.compressedCount(),r.freedBytes(),yn(r.pauseRequested()));
    }
    public Optional<BatRetentionRunSnapshot> findRun(String runId) {
        List<BatRetentionRunSnapshot> rows=jdbc.query("SELECT * FROM ops_retention_run WHERE run_id=?",(rs,n)->run(rs),runId);
        return rows.stream().findFirst();
    }
    public List<BatRetentionRunSnapshot> findRuns(String policyId,int limit) {
        if(policyId==null || policyId.isBlank()) {
            return jdbc.query(con->{var ps=con.prepareStatement("SELECT * FROM ops_retention_run ORDER BY started_at DESC,run_id DESC");ps.setMaxRows(limit);return ps;},(rs,n)->run(rs));
        }
        return jdbc.query(con->{var ps=con.prepareStatement("SELECT * FROM ops_retention_run WHERE policy_id=? ORDER BY started_at DESC,run_id DESC");ps.setString(1,policyId);ps.setMaxRows(limit);return ps;},(rs,n)->run(rs));
    }
    public void requestPause(String runId,String actor,String reason,long expectedVersion) {
        if (expectedVersion < 0) throw new IllegalArgumentException("expectedVersion은 0 이상이어야 합니다.");
        int updated=jdbc.update(
                "UPDATE ops_retention_run SET pause_requested_yn='Y',control_actor_id=?,control_reason=?,updated_at=CURRENT_TIMESTAMP " +
                        "WHERE run_id=? AND status='RUNNING' AND EXISTS (" +
                        "SELECT 1 FROM ops_retention_policy p WHERE p.policy_id=ops_retention_run.policy_id AND p.row_version=?)",
                actor,safe(reason),runId,expectedVersion);
        if(updated!=1) throw new IllegalStateException("RETENTION_RUN_STATE_OR_POLICY_VERSION_CONFLICT");
    }
    public boolean pauseRequested(String runId) {
        Boolean v=jdbc.queryForObject("SELECT CASE WHEN pause_requested_yn='Y' THEN 1 ELSE 0 END FROM ops_retention_run WHERE run_id=?",(rs,n)->rs.getInt(1)==1,runId);
        return Boolean.TRUE.equals(v);
    }
    public void markRunning(String runId,String runtime,String actor) {
        jdbc.update("UPDATE ops_retention_run SET status='RUNNING',runtime_instance_id=?,actor_id=?,control_actor_id=?,pause_requested_yn='N',completed_at=NULL,error_code=NULL,error_summary=NULL,updated_at=CURRENT_TIMESTAMP WHERE run_id=?",runtime,actor,actor,runId);
    }
    public void progress(String runId,long matched,long archived,long deleted,long processed,long compressed,long freed) {
        jdbc.update("UPDATE ops_retention_run SET matched_count=?,archived_count=?,deleted_count=?,processed_count=?,compressed_count=?,freed_bytes=?,updated_at=CURRENT_TIMESTAMP WHERE run_id=?",matched,archived,deleted,processed,compressed,freed,runId);
    }
    public void finish(String runId,String status,String errorCode,String summary) {
        jdbc.update("UPDATE ops_retention_run SET status=?,completed_at=CURRENT_TIMESTAMP,error_code=?,error_summary=?,updated_at=CURRENT_TIMESTAMP WHERE run_id=?",status,errorCode,safe(summary),runId);
    }

    public void audit(String operation,String targetType,String targetId,String requestedBy,String approvedBy,
                      String approvalRequestId,String reason,Long expectedVersion,String resultState) {
        jdbc.update("INSERT INTO ops_retention_control_audit(audit_id,operation_type,target_type,target_id,requested_by,approved_by,approval_request_id,reason_text,expected_version,result_state,created_at) VALUES(?,?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP)",
                java.util.UUID.randomUUID().toString(),operation,targetType,targetId,requestedBy,approvedBy,approvalRequestId,
                safe(reason),expectedVersion,resultState);
    }

    public List<Map<String,Object>> findAuditsByApprovalRequestId(String approvalRequestId) {
        if (approvalRequestId == null || approvalRequestId.isBlank()) return List.of();
        return jdbc.queryForList("SELECT audit_id AS auditId,operation_type AS operationType,target_type AS targetType," +
                "target_id AS targetId,requested_by AS requestedBy,approved_by AS approvedBy," +
                "approval_request_id AS approvalRequestId,reason_text AS reason,expected_version AS expectedVersion," +
                "result_state AS resultState,created_at AS createdAt FROM ops_retention_control_audit " +
                "WHERE approval_request_id=? ORDER BY created_at,audit_id", approvalRequestId.trim());
    }

    private static BatRetentionPolicyDefinition policy(java.sql.ResultSet rs) throws java.sql.SQLException {
        return new BatRetentionPolicyDefinition(rs.getString("policy_id"),rs.getString("target_name"),rs.getString("action_name"),rs.getInt("retention_days"),rs.getString("schedule_expression"),lt(rs.getString("maintenance_start")),lt(rs.getString("maintenance_end")),"Y".equals(rs.getString("enabled_yn")),"Y".equals(rs.getString("legal_hold_yn")),rs.getInt("chunk_size"),rs.getLong("throttle_millis"),rs.getLong("max_rows_per_run"),rs.getLong("max_runtime_seconds"),rs.getInt("lease_seconds"),rs.getLong("policy_version"),instant(rs.getTimestamp("next_run_at")),rs.getLong("row_version"));
    }
    private static BatRetentionRunSnapshot run(java.sql.ResultSet rs)throws java.sql.SQLException {return new BatRetentionRunSnapshot(rs.getString("run_id"),rs.getString("policy_id"),rs.getString("trigger_type"),rs.getString("status"),rs.getString("runtime_instance_id"),rs.getString("actor_id"),rs.getString("reason"),rs.getLong("policy_version"),instant(rs.getTimestamp("cutoff_at")),instant(rs.getTimestamp("started_at")),instant(rs.getTimestamp("completed_at")),rs.getLong("matched_count"),rs.getLong("archived_count"),rs.getLong("deleted_count"),rs.getLong("processed_count"),rs.getLong("compressed_count"),rs.getLong("freed_bytes"),"Y".equals(rs.getString("pause_requested_yn")),rs.getString("error_code"),rs.getString("error_summary"));}
    private static String yn(boolean b){return b?"Y":"N";} private static Timestamp ts(Instant v){return v==null?null:Timestamp.from(v);} private static Instant instant(Timestamp v){return v==null?null:v.toInstant();} private static String time(LocalTime v){return v==null?null:v.toString();} private static LocalTime lt(String v){return v==null||v.isBlank()?null:LocalTime.parse(v);} private static String safe(String s){return s==null?null:s.substring(0,Math.min(500,s.length()));}
}
