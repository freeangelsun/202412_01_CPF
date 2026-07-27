package com.cpf.batch.centercut.runner.internal;

import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
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
    private final CpfVendorSqlCatalog sql;

    public JdbcCenterCutClaimRepository(
            JdbcTemplate jdbc,
            JdbcCenterCutAdmissionControl admission,
            CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        this.jdbc=jdbc;
        this.admission=admission;
        this.sql= sqlCatalogProvider.forModule("bat");
    }

    @Transactional
    public int recoverExpiredToUnknown() {
        List<Map<String,Object>> rows =
                jdbc.queryForList(sql.required("centercut-claim-find-expired-running"));
        int count=0;
        for(Map<String,Object> row:rows){
            long id=((Number)row.get("center_cut_item_id")).longValue();
            int changed=jdbc.update(sql.required("centercut-claim-expire"),id);
            if(changed==1){
                int item=jdbc.update(sql.required("centercut-item-mark-unknown"),id);
                count+=item;
                if(item==1 && row.get("center_cut_execution_id")!=null) {
                    jdbc.update(sql.required("centercut-execution-increment-unknown"),
                            row.get("center_cut_execution_id"));
                }
            }
        }
        return count;
    }

    @Transactional
    public Optional<Claim> claim(String runner,String pool,Duration duration) {
        List<Map<String,Object>> candidates =
                jdbc.queryForList(sql.required("centercut-claim-find-candidates"));
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
        int n=jdbc.update(sql.required("centercut-claim-release-complete"),
                c.itemId(),c.runnerId(),c.claimToken(),c.fencingToken());
        if(n!=1) throw new IllegalStateException("Stale center-cut runner fenced; completion rejected");
        jdbc.update(sql.required("centercut-item-complete"),
                status,SensitiveTextSanitizer.sanitize(message),c.itemId());
        jdbc.update(sql.required("centercut-result-insert"),
                status,result,SensitiveTextSanitizer.sanitize(message),c.itemId());
        if(c.executionId()!=null&&!c.executionId().isBlank()){
            jdbc.update(sql.required("centercut-execution-update-counters"),
                    status,status,status,c.executionId());
            Integer remaining=jdbc.queryForObject(
                    sql.required("centercut-item-count-remaining"),
                    Integer.class,c.executionId());
            if(remaining!=null&&remaining==0) {
                jdbc.update(sql.required("centercut-execution-finalize"),c.executionId());
            }
        }
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
