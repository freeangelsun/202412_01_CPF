package com.cpf.batch.centercut.runner.internal;

import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Timestamp;
import java.time.*;
import java.util.*;

@Repository
public class JdbcCenterCutClaimRepository {
    private final JdbcTemplate jdbc;
    private final JdbcCenterCutAdmissionControl admission;
    private final CpfVendorSqlCatalog sql;
    private final TransactionOperations claimTransactions;

    @Autowired
    public JdbcCenterCutClaimRepository(
            JdbcTemplate jdbc,
            JdbcCenterCutAdmissionControl admission,
            CpfVendorSqlCatalogProvider sqlCatalogProvider,
            PlatformTransactionManager transactionManager) {
        this(jdbc, admission, sqlCatalogProvider, new TransactionTemplate(transactionManager));
    }

    JdbcCenterCutClaimRepository(
            JdbcTemplate jdbc,
            JdbcCenterCutAdmissionControl admission,
            CpfVendorSqlCatalogProvider sqlCatalogProvider,
            TransactionOperations claimTransactions) {
        this.jdbc=Objects.requireNonNull(jdbc, "jdbc");
        this.admission=Objects.requireNonNull(admission, "admission");
        this.sql=Objects.requireNonNull(sqlCatalogProvider, "sqlCatalogProvider").forModule("bat");
        this.claimTransactions=Objects.requireNonNull(claimTransactions, "claimTransactions");
    }

    /**
     * 만료 Claim을 Item별 독립 transaction으로 회수합니다.
     *
     * <p>하나의 손상된 Claim이 같은 주기의 정상 Claim 회수까지 계속 롤백시키는 poison-row
     * 장애를 막습니다. 성공 행은 커밋하고 실패 행은 롤백한 뒤, 처리된 수와 실패 식별자를
     * 포함한 예외를 던져 운영 경보와 후속 Reconcile이 누락되지 않게 합니다.</p>
     */
    public int recoverExpiredToUnknown() {
        List<Map<String,Object>> rows =
                jdbc.queryForList(sql.required("centercut-claim-find-expired-running"));
        int recovered=0;
        List<String> failures=new ArrayList<>();
        List<RuntimeException> causes=new ArrayList<>();
        for(Map<String,Object> row:rows){
            String itemRef=Objects.toString(row.get("center_cut_item_id"), "<missing>");
            try {
                long id=requiredItemId(row);
                Integer changed=claimTransactions.execute(status -> recoverExpiredRow(row, id));
                recovered+=changed==null?0:changed;
            } catch(RuntimeException failure) {
                failures.add(itemRef+":"+recoveryCode(failure));
                causes.add(failure);
            }
        }
        if(!failures.isEmpty()) {
            IllegalStateException partial=new IllegalStateException(
                    "CENTER_CUT_EXPIRED_CLAIM_RECOVERY_PARTIAL recovered="+recovered
                            +",failures="+String.join("|",failures));
            causes.forEach(partial::addSuppressed);
            throw partial;
        }
        return recovered;
    }

    private int recoverExpiredRow(Map<String,Object> row,long id) {
        int changed=jdbc.update(sql.required("centercut-claim-expire"),id);
        if(changed!=1) return 0;
        int item=jdbc.update(sql.required("centercut-item-mark-unknown"),id);
        if(item!=1) {
            throw new IllegalStateException("CENTER_CUT_ITEM_UNKNOWN_CONFLICT");
        }
        String executionId=Objects.toString(row.get("center_cut_execution_id"), "").trim();
        if(executionId.isEmpty()) {
            throw new IllegalStateException("CENTER_CUT_UNKNOWN_EXECUTION_ID_MISSING");
        }
        int executionChanged=jdbc.update(
                sql.required("centercut-execution-increment-unknown"), executionId);
        if(executionChanged!=1) {
            throw new IllegalStateException("CENTER_CUT_UNKNOWN_EXECUTION_STATE_CONFLICT");
        }
        return 1;
    }

    private static long requiredItemId(Map<String,Object> row) {
        Object value=row.get("center_cut_item_id");
        if(!(value instanceof Number number)) {
            throw new IllegalStateException("CENTER_CUT_EXPIRED_CLAIM_ITEM_ID_INVALID");
        }
        return number.longValue();
    }

    private static String recoveryCode(RuntimeException failure) {
        String message=Objects.toString(failure.getMessage(), failure.getClass().getSimpleName());
        return SensitiveTextSanitizer.sanitize(message).replaceAll("[\r\n\t|,]+", "_");
    }

    public Optional<Claim> claimForExecution(
            String requiredExecutionId,String runner,String pool,Duration duration) {
        if(requiredExecutionId==null||requiredExecutionId.isBlank()) {
            throw new IllegalArgumentException("Center-Cut execution ID is required");
        }
        List<Map<String,Object>> candidates =
                jdbc.queryForList(sql.required("centercut-claim-find-candidates"));
        for(Map<String,Object> row:candidates){
            String executionId=Objects.toString(row.get("center_cut_execution_id"),"");
            if(!requiredExecutionId.equals(executionId))continue;
            int tps=((Number)row.getOrDefault("tps_limit",0)).intValue();
            int concurrency=((Number)row.getOrDefault("concurrency_limit",1)).intValue();
            long itemId=((Number)row.get("center_cut_item_id")).longValue();
            Optional<Claim> claim=claimTransactions.execute(status ->
                    claimWithinTransaction(status, itemId, runner, pool, duration, executionId, tps, concurrency));
            if(claim!=null&&claim.isPresent())return claim;
        }
        return Optional.empty();
    }

    private Optional<Claim> claimWithinTransaction(
            TransactionStatus status,
            long itemId,
            String runner,
            String pool,
            Duration duration,
            String executionId,
            int tps,
            int concurrency) {
        if(!executionId.isBlank()&&!admission.acquire(executionId,tps,concurrency)) {
            status.setRollbackOnly();
            return Optional.empty();
        }
        Optional<Claim> claimed=tryClaim(itemId,runner,pool,duration,executionId);
        if(claimed.isEmpty()) {
            // Admission and claim are one atomic attempt. A normal empty result must not commit a TPS permit.
            status.setRollbackOnly();
        }
        return claimed;
    }

    @Transactional
    public Optional<Claim> tryClaim(long item,String runner,String pool,Duration duration,String executionId) {
        Instant now=Instant.now();String token=UUID.randomUUID().toString();
        List<Map<String,Object>> rows=jdbc.queryForList(
                sql.required("centercut-claim-find-fencing"),item);
        long fence=rows.isEmpty()?1:((Number)rows.get(0).get("fencing_token")).longValue()+1;
        int updated=jdbc.update(sql.required("centercut-claim-reclaim"),
                runner,pool,token,fence,Timestamp.from(now.plus(duration)),Timestamp.from(now),item);
        if(updated==0) try {
            jdbc.update(sql.required("centercut-claim-insert"),
                    item,runner,pool,token,Timestamp.from(now.plus(duration)),Timestamp.from(now));
            fence=1;
        } catch(DuplicateKeyException conflict){return Optional.empty();}
        if(jdbc.update(sql.required("centercut-item-mark-running"),item)!=1) {
            jdbc.update(sql.required("centercut-claim-release-after-item-conflict"),
                    item,runner,token,fence);
            return Optional.empty();
        }
        return Optional.of(new Claim(item,runner,token,fence,now.plus(duration),executionId));
    }

    public boolean renew(Claim claim,Duration duration) {
        return jdbc.update(sql.required("centercut-claim-renew"),
                Timestamp.from(Instant.now().plus(duration)),claim.itemId(),claim.runnerId(),
                claim.claimToken(),claim.fencingToken())==1;
    }

    public Work load(Claim c) {
        return jdbc.queryForObject(sql.required("centercut-item-load-work"),
                (rs,n)->new Work(rs.getLong(1),rs.getString(2),rs.getString(3),rs.getString(4),
                        rs.getString(5),rs.getString(6),rs.getString(7),rs.getString(8),rs.getString(9)),
                c.itemId());
    }

    @Transactional
    public void complete(Claim c,String status,String result,String message) {
        int claimChanged=jdbc.update(sql.required("centercut-claim-release-complete"),
                c.itemId(),c.runnerId(),c.claimToken(),c.fencingToken());
        requireSingleRow(claimChanged, "CENTER_CUT_STALE_RUNNER_FENCED");
        String sanitized=SensitiveTextSanitizer.sanitize(message);
        int itemChanged=jdbc.update(sql.required("centercut-item-complete"),
                status,sanitized,c.itemId());
        requireSingleRow(itemChanged, "CENTER_CUT_ITEM_COMPLETION_CONFLICT");
        int resultInserted=jdbc.update(sql.required("centercut-result-insert"),
                status,result,sanitized,c.itemId());
        requireSingleRow(resultInserted, "CENTER_CUT_RESULT_PERSISTENCE_CONFLICT");
        if(c.executionId()!=null&&!c.executionId().isBlank()&&!"RETRY".equals(status)){
            int executionChanged=jdbc.update(sql.required("centercut-execution-update-counters"),
                    status,status,status,c.executionId());
            requireSingleRow(executionChanged, "CENTER_CUT_EXECUTION_COUNTER_CONFLICT");
            Integer remaining=jdbc.queryForObject(
                    sql.required("centercut-item-count-remaining"),
                    Integer.class,c.executionId());
            if(remaining==null) {
                throw new IllegalStateException("CENTER_CUT_REMAINING_COUNT_UNAVAILABLE");
            }
            if(remaining==0) {
                int finalized=jdbc.update(
                        sql.required("centercut-execution-finalize"),c.executionId());
                requireSingleRow(finalized, "CENTER_CUT_EXECUTION_FINALIZE_CONFLICT");
            }
        }
    }

    private static void requireSingleRow(int changed,String code) {
        if(changed!=1) throw new IllegalStateException(code);
    }

    public int requeueFailed(String jobId) {
        return jdbc.update(sql.required("centercut-item-requeue-failed"),jobId);
    }

    public int reconcileUnknown(String jobId) {
        return jdbc.update(sql.required("centercut-item-reconcile-unknown"),jobId);
    }

    public record Work(long itemId,String executionId,String businessKey,String payload,String centerCutJobId,String transactionId,String segmentId,String handlerKey,String jobCode) {}
    public record Claim(long itemId,String runnerId,String claimToken,long fencingToken,Instant leaseUntil,String executionId) {}
}
