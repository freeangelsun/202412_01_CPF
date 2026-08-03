package com.cpf.starter.integration.resilience.internal;

import com.cpf.core.api.resilience.CpfResiliencePolicy;
import com.cpf.core.spi.resilience.CpfResiliencePolicyStore;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/** JDBC policy store with active/pending uniqueness and atomic approval. */
public final class JdbcCpfResiliencePolicyStore implements CpfResiliencePolicyStore {
    private final JdbcTemplate jdbc; private final TransactionTemplate tx;
    public JdbcCpfResiliencePolicyStore(JdbcTemplate jdbc, TransactionTemplate tx){this.jdbc=jdbc;this.tx=tx;}
    @Override public Optional<CpfResiliencePolicy> findActive(String operationId){
        var rows=jdbc.query("select operation_id,revision,timeout_ms,max_attempts,retry_backoff_ms,circuit_failure_threshold,circuit_open_ms,bulkhead_max_concurrent,rate_limit_permits,rate_limit_window_ms,idempotent_flag,reconcile_flag from cpf_resilience_policy where operation_id=? and policy_status='ACTIVE'",this::map,operationId);
        return rows.stream().findFirst();
    }
    @Override public List<CpfResiliencePolicy> search(String filter,int offset,int limit){
        String value="%"+(filter==null?"":filter.trim())+"%";
        int end=Math.addExact(offset,limit); String sql="select operation_id,revision,timeout_ms,max_attempts,retry_backoff_ms,circuit_failure_threshold,circuit_open_ms,bulkhead_max_concurrent,rate_limit_permits,rate_limit_window_ms,idempotent_flag,reconcile_flag from (select operation_id,revision,timeout_ms,max_attempts,retry_backoff_ms,circuit_failure_threshold,circuit_open_ms,bulkhead_max_concurrent,rate_limit_permits,rate_limit_window_ms,idempotent_flag,reconcile_flag,row_number() over(order by operation_id) cpf_rn from cpf_resilience_policy where policy_status='ACTIVE' and operation_id like ?) cpf_page where cpf_rn>? and cpf_rn<=? order by cpf_rn";
        return jdbc.query(sql,this::map,value,offset,end);
    }
    @Override public String request(CpfResiliencePolicy p,String requester,String reason){
        String id=UUID.randomUUID().toString();
        try { jdbc.update("insert into cpf_resilience_policy_request(request_id,operation_id,requested_revision,policy_payload,requester_id,request_reason,request_status,active_operation_key) values(?,?,?,?,?,?, 'PENDING',?)",id,p.operationId(),p.revision(),encode(p),requester,reason,p.operationId()); }
        catch(DuplicateKeyException e){throw new IllegalStateException("pending policy request already exists: "+p.operationId(),e);} return id;
    }
    @Override public CpfResiliencePolicy approve(String requestId,String approver,String reason){
        return tx.execute(status -> {
            var rows=jdbc.query("select operation_id,policy_payload,requester_id from cpf_resilience_policy_request where request_id=? and request_status='PENDING' for update",(rs,n)->new RequestRow(rs.getString(1),rs.getString(2),rs.getString(3)),requestId);
            if(rows.size()!=1)throw new IllegalArgumentException("pending request not found"); var row=rows.getFirst();
            if(row.requesterId.equals(approver))throw new IllegalArgumentException("self approval is forbidden");
            long revision=Optional.ofNullable(jdbc.queryForObject("select max(revision) from cpf_resilience_policy where operation_id=?",Long.class,row.operationId)).orElse(0L)+1;
            CpfResiliencePolicy p=decode(row.payload,revision);
            jdbc.update("update cpf_resilience_policy set policy_status='SUPERSEDED',active_operation_key=null,updated_by=?,updated_at=CURRENT_TIMESTAMP where operation_id=? and policy_status='ACTIVE'",approver,row.operationId);
            insertActive(p,approver);
            int updated=jdbc.update("update cpf_resilience_policy_request set request_status='APPROVED',approver_id=?,approval_reason=?,active_operation_key=null where request_id=? and request_status='PENDING'",approver,reason,requestId);
            if(updated!=1)throw new IllegalStateException("approval conflict"); return p;
        });
    }
    @Override public void reject(String requestId,String approver,String reason){
        int n=jdbc.update("update cpf_resilience_policy_request set request_status='REJECTED',approver_id=?,approval_reason=?,active_operation_key=null where request_id=? and request_status='PENDING' and requester_id<>?",approver,reason,requestId,approver);
        if(n!=1)throw new IllegalArgumentException("request missing, conflicted or self-approved");
    }
    private void insertActive(CpfResiliencePolicy p,String operator){jdbc.update("insert into cpf_resilience_policy(policy_id,operation_id,revision,timeout_ms,max_attempts,retry_backoff_ms,circuit_failure_threshold,circuit_open_ms,bulkhead_max_concurrent,rate_limit_permits,rate_limit_window_ms,idempotent_flag,reconcile_flag,policy_status,active_operation_key,updated_by,updated_at) values(?,?,?,?,?,?,?,?,?,?,?,?,?,'ACTIVE',?,?,CURRENT_TIMESTAMP)",UUID.randomUUID().toString(),p.operationId(),p.revision(),p.timeoutBudget().toMillis(),p.maxAttempts(),p.retryBackoff().toMillis(),p.circuitFailureThreshold(),p.circuitOpenDuration().toMillis(),p.bulkheadMaxConcurrent(),p.rateLimitPermits(),p.rateLimitWindow().toMillis(),p.idempotent()?"Y":"N",p.unknownResultReconcileEnabled()?"Y":"N",p.operationId(),operator);}
    private CpfResiliencePolicy map(ResultSet r,int n)throws SQLException{return new CpfResiliencePolicy(r.getString(1),r.getLong(2),Duration.ofMillis(r.getLong(3)),r.getInt(4),Duration.ofMillis(r.getLong(5)),r.getInt(6),Duration.ofMillis(r.getLong(7)),r.getInt(8),r.getInt(9),Duration.ofMillis(r.getLong(10)),"Y".equals(r.getString(11)),"Y".equals(r.getString(12)));}
    private static String encode(CpfResiliencePolicy p){return String.join("|",p.operationId(),Long.toString(p.timeoutBudget().toMillis()),Integer.toString(p.maxAttempts()),Long.toString(p.retryBackoff().toMillis()),Integer.toString(p.circuitFailureThreshold()),Long.toString(p.circuitOpenDuration().toMillis()),Integer.toString(p.bulkheadMaxConcurrent()),Integer.toString(p.rateLimitPermits()),Long.toString(p.rateLimitWindow().toMillis()),p.idempotent()?"Y":"N",p.unknownResultReconcileEnabled()?"Y":"N");}
    private static CpfResiliencePolicy decode(String v,long rev){String[]x=v.split("\\|",-1);return new CpfResiliencePolicy(x[0],rev,Duration.ofMillis(Long.parseLong(x[1])),Integer.parseInt(x[2]),Duration.ofMillis(Long.parseLong(x[3])),Integer.parseInt(x[4]),Duration.ofMillis(Long.parseLong(x[5])),Integer.parseInt(x[6]),Integer.parseInt(x[7]),Duration.ofMillis(Long.parseLong(x[8])),"Y".equals(x[9]),"Y".equals(x[10]));}
    private record RequestRow(String operationId,String payload,String requesterId){}
}
