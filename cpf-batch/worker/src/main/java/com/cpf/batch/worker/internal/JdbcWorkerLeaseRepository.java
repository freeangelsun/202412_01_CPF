package com.cpf.batch.worker.internal;

import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.core.common.database.CpfVendorSqlCatalog;
import org.springframework.core.env.Environment;
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
    private final CpfVendorSqlCatalog sql;

    public JdbcWorkerLeaseRepository(JdbcTemplate jdbc, Environment environment) {
        this.jdbc = jdbc;
        this.sql = CpfVendorSqlCatalog.create(environment, "bat");
    }

    @Transactional
    public RecoveryResult recoverExpired() {
        int safeRequeued = 0;
        int unknown = 0;
        List<Map<String,Object>> rows =
            jdbc.queryForList(sql.required("worker-lease-find-expired"));
        for (Map<String,Object> row : rows) {
            long id=((Number)row.get("execution_id")).longValue();
            String leaseStatus=Objects.toString(row.get("lease_status"),"");
            String executionStatus=Objects.toString(row.get("execution_status"),"");
            int expired=jdbc.update(sql.required("worker-lease-expire"),
                id,row.get("lease_token"),row.get("fencing_token"));
            if (expired!=1) continue;
            if ("CLAIMED".equals(leaseStatus) && Set.of("CLAIMING","CLAIMED").contains(executionStatus)) {
                safeRequeued += jdbc.update(
                    sql.required("worker-execution-requeue-expired-claim"), id);
            } else {
                unknown += jdbc.update(sql.required("worker-execution-mark-unknown"), id);
            }
        }
        return new RecoveryResult(safeRequeued,unknown);
    }

    @Transactional
    public Optional<Lease> claim(String worker,String version,List<String> capabilities,Duration duration) {
        Set<String> capabilitySet=new HashSet<>(capabilities);
        for (Map<String,Object> row:jdbc.queryForList(
                sql.required("worker-execution-find-ready-candidates"))) {
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
        if(jdbc.update(sql.required("worker-execution-mark-claiming"),
                worker,Timestamp.from(now),executionId)!=1) return Optional.empty();
        List<Map<String,Object>> rows=jdbc.queryForList(
            sql.required("worker-lease-find-fencing"),executionId);
        long fence=rows.isEmpty()?1:((Number)rows.get(0).get("fencing_token")).longValue()+1;
        try {
            int updated=jdbc.update(sql.required("worker-lease-reclaim"),
                worker,token,Timestamp.from(now),Timestamp.from(until),Timestamp.from(now),fence,executionId);
            if(updated==0){
                jdbc.update(sql.required("worker-lease-insert"),
                    executionId,worker,token,Timestamp.from(now),Timestamp.from(until),Timestamp.from(now));
                fence=1;
            }
        } catch(DuplicateKeyException conflict) {
            jdbc.update(sql.required("worker-execution-revert-claim"),executionId);
            return Optional.empty();
        }
        jdbc.update(sql.required("worker-execution-mark-claimed"),
            worker,Timestamp.from(now),executionId);
        return Optional.of(new Lease(executionId,worker,token,fence,until));
    }

    public boolean renew(Lease lease,Duration duration) {
        return jdbc.update(sql.required("worker-lease-renew"),
            Timestamp.from(Instant.now().plus(duration)),lease.executionId(),lease.workerId(),
            lease.leaseToken(),lease.fencingToken())==1;
    }

    public void complete(Lease lease,String status,String message) {
        int n=jdbc.update(sql.required("worker-lease-release"),
            SensitiveTextSanitizer.sanitize(message),lease.executionId(),lease.workerId(),
            lease.leaseToken(),lease.fencingToken());
        if(n!=1) throw new IllegalStateException("Stale worker fenced; completion rejected");
        jdbc.update(sql.required("worker-execution-clear-worker"),lease.executionId());
    }

    public record Lease(long executionId,String workerId,String leaseToken,long fencingToken,Instant leaseUntil) {}
    public record RecoveryResult(int safeRequeued,int unknownResult) {}
}
