package com.cpf.batch.worker.internal;

import com.cpf.batch.runtime.SensitiveTextSanitizer;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Repository
public class JdbcWorkerLeaseRepository {
    private final JdbcTemplate jdbc;
    public JdbcWorkerLeaseRepository(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Transactional
    public RecoveryResult recoverExpired() {
        int safeRequeued = 0;
        int unknown = 0;
        List<Map<String,Object>> rows = jdbc.queryForList("""
            SELECT l.execution_id,l.worker_id,l.lease_token,l.fencing_token,l.lease_status,e.execution_status
              FROM bat_execution_lease l JOIN bat_execution e ON e.execution_id=l.execution_id
             WHERE l.lease_status IN ('CLAIMED','RUNNING') AND l.lease_until<CURRENT_TIMESTAMP(3)
             ORDER BY l.execution_id
            """);
        for (Map<String,Object> row : rows) {
            long id=((Number)row.get("execution_id")).longValue();
            String leaseStatus=Objects.toString(row.get("lease_status"),"");
            String executionStatus=Objects.toString(row.get("execution_status"),"");
            int expired=jdbc.update("""
                UPDATE bat_execution_lease SET lease_status='EXPIRED',released_at=CURRENT_TIMESTAMP(3)
                 WHERE execution_id=? AND lease_token=? AND fencing_token=? AND lease_until<CURRENT_TIMESTAMP(3)
                   AND lease_status IN ('CLAIMED','RUNNING')
                """, id,row.get("lease_token"),row.get("fencing_token"));
            if (expired!=1) continue;
            if ("CLAIMED".equals(leaseStatus) && Set.of("CLAIMING","CLAIMED").contains(executionStatus)) {
                safeRequeued += jdbc.update("""
                    UPDATE bat_execution SET execution_status='READY',worker_id=NULL,last_heartbeat_at=NULL,
                           error_message=NULL,updated_at=CURRENT_TIMESTAMP
                     WHERE execution_id=? AND execution_status IN ('CLAIMING','CLAIMED')
                    """, id);
            } else {
                unknown += jdbc.update("""
                    UPDATE bat_execution SET execution_status='UNKNOWN_RESULT',worker_id=NULL,end_time=CURRENT_TIMESTAMP(3),
                           error_message='Worker lease expired after execution may have started; reconcile before retry',
                           updated_at=CURRENT_TIMESTAMP
                     WHERE execution_id=? AND execution_status IN ('RUNNING','CLAIMING','CLAIMED')
                    """, id);
            }
        }
        return new RecoveryResult(safeRequeued,unknown);
    }

    @Transactional
    public Optional<Lease> claim(String worker,String version,List<String> capabilities,Duration duration) {
        Set<String> capabilitySet=new HashSet<>(capabilities);
        for (Map<String,Object> row:jdbc.queryForList("""
            SELECT execution_id,required_worker_version,required_capability
              FROM bat_execution WHERE execution_status='READY' AND stop_requested_yn='N'
             ORDER BY execution_id LIMIT 100
            """)) {
            String requiredVersion=Objects.toString(row.get("required_worker_version"),"");
            String requiredCapability=Objects.toString(row.get("required_capability"),"");
            if(!requiredVersion.isBlank()&&!requiredVersion.equals(version)) continue;
            if(!requiredCapability.isBlank()&&!capabilitySet.contains(requiredCapability)) continue;
            Optional<Lease> lease=tryClaim(((Number)row.get("execution_id")).longValue(),worker,duration);
            if(lease.isPresent()) return lease;
        }
        return Optional.empty();
    }

    @Transactional
    public Optional<Lease> tryClaim(long executionId,String worker,Duration duration) {
        Instant now=Instant.now(),until=now.plus(duration);String token=UUID.randomUUID().toString();
        if(jdbc.update("""
            UPDATE bat_execution SET execution_status='CLAIMING',worker_id=?,last_heartbeat_at=?
             WHERE execution_id=? AND execution_status='READY' AND stop_requested_yn='N'
            """,worker,Timestamp.from(now),executionId)!=1) return Optional.empty();
        List<Map<String,Object>> rows=jdbc.queryForList("SELECT fencing_token FROM bat_execution_lease WHERE execution_id=?",executionId);
        long fence=rows.isEmpty()?1:((Number)rows.get(0).get("fencing_token")).longValue()+1;
        try {
            int updated=jdbc.update("""
                UPDATE bat_execution_lease SET worker_id=?,lease_token=?,lease_status='CLAIMED',claimed_at=?,lease_until=?,
                       last_heartbeat_at=?,attempt_no=attempt_no+1,takeover_count=takeover_count+1,fencing_token=?,
                       released_at=NULL,failure_message=NULL
                 WHERE execution_id=? AND lease_status IN ('RELEASED','EXPIRED')
                """,worker,token,Timestamp.from(now),Timestamp.from(until),Timestamp.from(now),fence,executionId);
            if(updated==0){
                jdbc.update("""
                    INSERT INTO bat_execution_lease(execution_id,worker_id,lease_token,lease_status,claimed_at,lease_until,
                      last_heartbeat_at,attempt_no,takeover_count,fencing_token)
                    VALUES(?,?,?,'CLAIMED',?,?,?,1,0,1)
                    """,executionId,worker,token,Timestamp.from(now),Timestamp.from(until),Timestamp.from(now));
                fence=1;
            }
        } catch(DuplicateKeyException conflict) {
            jdbc.update("UPDATE bat_execution SET execution_status='READY',worker_id=NULL WHERE execution_id=? AND execution_status='CLAIMING'",executionId);
            return Optional.empty();
        }
        jdbc.update("""
            UPDATE bat_execution SET execution_status='CLAIMED',worker_id=?,last_heartbeat_at=?
             WHERE execution_id=? AND execution_status='CLAIMING'
            """,worker,Timestamp.from(now),executionId);
        return Optional.of(new Lease(executionId,worker,token,fence,until));
    }

    public boolean renew(Lease lease,Duration duration) {
        return jdbc.update("""
            UPDATE bat_execution_lease SET lease_until=?,last_heartbeat_at=CURRENT_TIMESTAMP(3),lease_status='RUNNING'
             WHERE execution_id=? AND worker_id=? AND lease_token=? AND fencing_token=?
               AND lease_until>=CURRENT_TIMESTAMP(3) AND lease_status IN ('CLAIMED','RUNNING')
            """,Timestamp.from(Instant.now().plus(duration)),lease.executionId(),lease.workerId(),lease.leaseToken(),lease.fencingToken())==1;
    }

    public void complete(Lease lease,String status,String message) {
        int n=jdbc.update("""
            UPDATE bat_execution_lease SET lease_status='RELEASED',released_at=CURRENT_TIMESTAMP(3),failure_message=?
             WHERE execution_id=? AND worker_id=? AND lease_token=? AND fencing_token=? AND lease_status IN ('CLAIMED','RUNNING')
            """,SensitiveTextSanitizer.sanitize(message),lease.executionId(),lease.workerId(),lease.leaseToken(),lease.fencingToken());
        if(n!=1) throw new IllegalStateException("Stale worker fenced; completion rejected");
        jdbc.update("UPDATE bat_execution SET worker_id=NULL,updated_at=CURRENT_TIMESTAMP WHERE execution_id=?",lease.executionId());
    }

    public record Lease(long executionId,String workerId,String leaseToken,long fencingToken,Instant leaseUntil) {}
    public record RecoveryResult(int safeRequeued,int unknownResult) {}
}
