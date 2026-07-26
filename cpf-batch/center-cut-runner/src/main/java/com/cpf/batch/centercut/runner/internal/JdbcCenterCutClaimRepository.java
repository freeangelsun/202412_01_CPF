package com.cpf.batch.centercut.runner.internal;

import com.cpf.batch.runtime.SensitiveTextSanitizer;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;

@Repository
public class JdbcCenterCutClaimRepository {
    private final JdbcTemplate jdbc;
    private final JdbcCenterCutAdmissionControl admission;
    public JdbcCenterCutClaimRepository(JdbcTemplate jdbc,JdbcCenterCutAdmissionControl admission){this.jdbc=jdbc;this.admission=admission;}

    @Transactional
    public int recoverExpiredToUnknown() {
        List<Map<String,Object>> rows=jdbc.queryForList("""
          SELECT c.center_cut_item_id,i.center_cut_execution_id
            FROM bat_center_cut_claim c JOIN bat_center_cut_item i ON i.center_cut_item_id=c.center_cut_item_id
           WHERE c.claim_status IN ('CLAIMED','RUNNING') AND c.lease_until<CURRENT_TIMESTAMP(6) AND i.item_status='RUNNING'
          """);
        int count=0;
        for(Map<String,Object> row:rows){
            long id=((Number)row.get("center_cut_item_id")).longValue();
            int changed=jdbc.update("""
              UPDATE bat_center_cut_claim SET claim_status='EXPIRED',released_at=CURRENT_TIMESTAMP(6)
               WHERE center_cut_item_id=? AND claim_status IN ('CLAIMED','RUNNING') AND lease_until<CURRENT_TIMESTAMP(6)
              """,id);
            if(changed==1){
                int item=jdbc.update("""
                  UPDATE bat_center_cut_item SET item_status='UNKNOWN_RESULT',completed_at=CURRENT_TIMESTAMP(3),
                    last_error_message='Center-Cut lease expired; reconcile before retry',updated_at=CURRENT_TIMESTAMP
                   WHERE center_cut_item_id=? AND item_status='RUNNING'
                  """,id);
                count+=item;
                if(item==1 && row.get("center_cut_execution_id")!=null) jdbc.update("""
                  UPDATE bat_center_cut_execution SET unknown_count=unknown_count+1,execution_state='UNKNOWN_RESULT',
                         last_error_message='One or more items became UNKNOWN_RESULT',updated_at=CURRENT_TIMESTAMP(6)
                   WHERE center_cut_execution_id=?
                  """,row.get("center_cut_execution_id"));
            }
        }
        return count;
    }

    @Transactional
    public Optional<Claim> claim(String runner,String pool,Duration duration) {
        List<Map<String,Object>> candidates=jdbc.queryForList("""
          SELECT i.center_cut_item_id,i.center_cut_execution_id,e.tps_limit,e.concurrency_limit
            FROM bat_center_cut_item i
            JOIN bat_center_cut_job j ON j.center_cut_job_id=i.center_cut_job_id
            LEFT JOIN bat_center_cut_execution e ON e.center_cut_execution_id=i.center_cut_execution_id
           WHERE i.item_status IN ('READY','RETRY') AND j.use_yn='Y'
             AND (e.center_cut_execution_id IS NULL OR e.execution_state='RUNNING')
           ORDER BY i.center_cut_item_id LIMIT 100
          """);
        for(Map<String,Object> row:candidates){
            String executionId=Objects.toString(row.get("center_cut_execution_id"),"");
            if(!executionId.isBlank()){
                int tps=((Number)row.getOrDefault("tps_limit",0)).intValue();
                int concurrency=((Number)row.getOrDefault("concurrency_limit",1)).intValue();
                if(!admission.acquire(executionId,tps,concurrency))continue;
            }
            Optional<Claim> claim=tryClaim(((Number)row.get("center_cut_item_id")).longValue(),runner,pool,duration,executionId);
            if(claim.isPresent())return claim;
        }
        return Optional.empty();
    }

    @Transactional
    public Optional<Claim> tryClaim(long item,String runner,String pool,Duration duration,String executionId) {
        Instant now=Instant.now();String token=UUID.randomUUID().toString();
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT fencing_token FROM bat_center_cut_claim WHERE center_cut_item_id=?",item);
        long fence=rows.isEmpty()?1:((Number)rows.get(0).get("fencing_token")).longValue()+1;
        int updated=jdbc.update("""
          UPDATE bat_center_cut_claim SET runner_id=?,pool_id=?,claim_token=?,claim_status='CLAIMED',fencing_token=?,lease_until=?,
            last_heartbeat_at=?,attempt_no=attempt_no+1,takeover_count=takeover_count+1,released_at=NULL
           WHERE center_cut_item_id=? AND claim_status IN ('RELEASED','EXPIRED')
          """,runner,pool,token,fence,Timestamp.from(now.plus(duration)),Timestamp.from(now),item);
        if(updated==0) try {
            jdbc.update("""
              INSERT INTO bat_center_cut_claim(center_cut_item_id,runner_id,pool_id,claim_token,claim_status,fencing_token,
                lease_until,last_heartbeat_at,attempt_no,takeover_count) VALUES(?,?,?,?,'CLAIMED',1,?,?,1,0)
              """,item,runner,pool,token,Timestamp.from(now.plus(duration)),Timestamp.from(now)); fence=1;
        } catch(DuplicateKeyException conflict){return Optional.empty();}
        if(jdbc.update("""
          UPDATE bat_center_cut_item SET item_status='RUNNING',started_at=COALESCE(started_at,CURRENT_TIMESTAMP(3)),updated_at=CURRENT_TIMESTAMP
           WHERE center_cut_item_id=? AND item_status IN ('READY','RETRY')
          """,item)!=1) return Optional.empty();
        return Optional.of(new Claim(item,runner,token,fence,now.plus(duration),executionId));
    }

    public boolean renew(Claim claim,Duration duration) {
        return jdbc.update("""
          UPDATE bat_center_cut_claim SET lease_until=?,last_heartbeat_at=CURRENT_TIMESTAMP(6),claim_status='RUNNING'
           WHERE center_cut_item_id=? AND runner_id=? AND claim_token=? AND fencing_token=?
             AND lease_until>=CURRENT_TIMESTAMP(6) AND claim_status IN ('CLAIMED','RUNNING')
          """,Timestamp.from(Instant.now().plus(duration)),claim.itemId(),claim.runnerId(),claim.claimToken(),claim.fencingToken())==1;
    }

    public Work load(Claim c) {
        return jdbc.queryForObject("""
          SELECT i.center_cut_item_id,i.center_cut_execution_id,i.business_key,i.item_payload,i.center_cut_job_id,
                 i.transaction_id,i.transaction_segment_id,j.handler_key,COALESCE(j.batch_job_id,j.center_cut_job_id) job_code
            FROM bat_center_cut_item i JOIN bat_center_cut_job j ON j.center_cut_job_id=i.center_cut_job_id
           WHERE i.center_cut_item_id=?
          """,(rs,n)->new Work(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4),rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8),rs.getString(9)),c.itemId());
    }

    @Transactional
    public void complete(Claim c,String status,String result,String message) {
        int n=jdbc.update("""
          UPDATE bat_center_cut_claim SET claim_status='RELEASED',released_at=CURRENT_TIMESTAMP(6)
           WHERE center_cut_item_id=? AND runner_id=? AND claim_token=? AND fencing_token=? AND claim_status IN ('CLAIMED','RUNNING')
          """,c.itemId(),c.runnerId(),c.claimToken(),c.fencingToken());
        if(n!=1) throw new IllegalStateException("Stale center-cut runner fenced; completion rejected");
        jdbc.update("UPDATE bat_center_cut_item SET item_status=?,completed_at=CURRENT_TIMESTAMP(3),last_error_message=?,updated_at=CURRENT_TIMESTAMP WHERE center_cut_item_id=?",
          status,SensitiveTextSanitizer.sanitize(message),c.itemId());
        jdbc.update("""
          INSERT INTO bat_center_cut_result(center_cut_item_id,center_cut_job_id,result_status,result_payload,result_message,
             transaction_id,transaction_segment_id,parent_segment_id,created_by,updated_by)
          SELECT center_cut_item_id,center_cut_job_id,?,?,?,transaction_id,transaction_segment_id,parent_segment_id,
             'CENTER_CUT_RUNNER','CENTER_CUT_RUNNER' FROM bat_center_cut_item WHERE center_cut_item_id=?
          """,status,result,SensitiveTextSanitizer.sanitize(message),c.itemId());
        if(c.executionId()!=null&&!c.executionId().isBlank()){
            String column=switch(status){case "SUCCESS","COMPLETED"->"success_count";case "UNKNOWN_RESULT"->"unknown_count";default->"failure_count";};
            jdbc.update("UPDATE bat_center_cut_execution SET processed_count=processed_count+1,"+column+"="+column+"+1,updated_at=CURRENT_TIMESTAMP(6) WHERE center_cut_execution_id=?",c.executionId());
            Integer remaining=jdbc.queryForObject("SELECT COUNT(*) FROM bat_center_cut_item WHERE center_cut_execution_id=? AND item_status IN ('READY','RETRY','RUNNING')",Integer.class,c.executionId());
            if(remaining!=null&&remaining==0)jdbc.update("""
              UPDATE bat_center_cut_execution SET execution_state=CASE
                       WHEN execution_state='CANCELLED' THEN 'CANCELLED'
                       WHEN execution_state IN ('PAUSED','DRAINING') THEN execution_state
                       WHEN unknown_count>0 THEN 'UNKNOWN_RESULT'
                       WHEN failure_count>0 THEN 'FAILED'
                       ELSE 'COMPLETED' END,
                     completed_at=CASE WHEN execution_state IN ('CANCELLED','PAUSED','DRAINING') THEN completed_at ELSE CURRENT_TIMESTAMP(6) END,
                     updated_at=CURRENT_TIMESTAMP(6) WHERE center_cut_execution_id=?
              """,c.executionId());
        }
    }

    public int requeueFailed(String jobId) { return jdbc.update("""
      UPDATE bat_center_cut_item SET item_status='RETRY',retry_count=retry_count+1,completed_at=NULL,updated_at=CURRENT_TIMESTAMP
       WHERE center_cut_job_id=? AND item_status='FAILED'
      """,jobId); }
    public int reconcileUnknown(String jobId) { return jdbc.update("""
      UPDATE bat_center_cut_item SET item_status='RETRY',retry_count=retry_count+1,completed_at=NULL,updated_at=CURRENT_TIMESTAMP
       WHERE center_cut_job_id=? AND item_status='UNKNOWN_RESULT'
      """,jobId); }

    public record Work(long itemId,String executionId,String businessKey,String payload,String centerCutJobId,String transactionId,String segmentId,String handlerKey,String jobCode) {}
    public record Claim(long itemId,String runnerId,String claimToken,long fencingToken,Instant leaseUntil,String executionId) {}
}
