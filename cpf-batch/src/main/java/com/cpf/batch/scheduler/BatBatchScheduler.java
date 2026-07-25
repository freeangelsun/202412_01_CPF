package com.cpf.batch.scheduler;

import com.cpf.batch.runtime.BatBatchLockManager;
import com.cpf.core.api.batch.CpfBatchExecutionRequest;
import com.cpf.core.api.batch.CpfBatchExecutionResult;
import com.cpf.batch.runtime.BatBatchLauncher;
import com.cpf.core.common.logging.SensitiveDataMasker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * BAT Owner가 실행하는 Cluster-safe Scheduler.
 * ADM은 이 클래스를 소유하거나 batDB를 직접 접근하지 않고 BAT operation contract를 호출합니다.
 */
@Component
public class BatBatchScheduler {
    private final BatBatchScheduleService scheduleService;
    private final BatBatchLockManager lockService;
    private final BatBatchExecutionTargetService targetService;
    private final BatBatchLauncher batchLauncher;
    private final boolean enabled;
    private final int lockTtlSeconds;
    private final String ownerId;

    public BatBatchScheduler(
            BatBatchScheduleService scheduleService,
            BatBatchLockManager lockService,
            BatBatchExecutionTargetService targetService,
            BatBatchLauncher batchLauncher,
            @Value("${cpf.batch.scheduler.enabled:false}") boolean enabled,
            @Value("${cpf.batch.scheduler.lock-ttl-seconds:600}") int lockTtlSeconds) {
        this.scheduleService=scheduleService;this.lockService=lockService;this.targetService=targetService;
        this.batchLauncher=batchLauncher;this.enabled=enabled;this.lockTtlSeconds=Math.max(30,lockTtlSeconds);this.ownerId=ownerId();
    }

    @Scheduled(initialDelayString="${cpf.batch.scheduler.initial-delay-ms:30000}",
            fixedDelayString="${cpf.batch.scheduler.fixed-delay-ms:60000}")
    public void tick(){if(enabled)runOnce("BAT_SCHEDULER");}

    public List<Map<String,Object>> runOnce(String requestUser){
        List<Map<String,Object>> results=new ArrayList<>();
        for(BatBatchScheduleCandidate c:scheduleService.findDueSchedules(LocalDateTime.now()))results.add(runCandidate(c,requestUser));
        return results;
    }

    private Map<String,Object> runCandidate(BatBatchScheduleCandidate c,String requestUser){
        String lockKey=lockService.scheduleLockKey(c.scheduleId(),String.valueOf(c.plannedRunAt()));
        Map<String,Object> out=new LinkedHashMap<>();out.put("scheduleId",c.scheduleId());out.put("jobId",c.jobId());out.put("plannedRunAt",String.valueOf(c.plannedRunAt()));
        if(!lockService.acquire(lockKey,c.jobId(),c.jobParameters(),ownerId,lockTtlSeconds)){out.put("status","SKIPPED_LOCKED");return out;}
        Long targetId=null;
        try{
            targetId=targetService.createWaitingTarget(c,requestUser);
            CpfBatchExecutionResult execution=batchLauncher.run(CpfBatchExecutionRequest.scheduledRun(c.scheduleId(),c.jobId(),c.jobParameters(),requestUser,"BAT Scheduler 실행"));
            Long executionId=execution.cpfExecutionId();
            targetService.markDispatched(targetId,executionId,requestUser);scheduleService.updateFireTimes(c,requestUser);
            out.put("targetId",targetId);out.put("executionId",executionId);out.put("status",execution.status());return out;
        }catch(RuntimeException ex){
            String message=SensitiveDataMasker.mask(ex.getMessage(),500);if(targetId!=null)targetService.markFailed(targetId,message,requestUser);
            out.put("targetId",targetId);out.put("status","FAILED");out.put("message",message);return out;
        }finally{lockService.release(lockKey,ownerId);}
    }

    private String ownerId(){try{return InetAddress.getLocalHost().getHostName()+":"+ManagementFactory.getRuntimeMXBean().getName();}catch(Exception ex){return "bat-scheduler";}}
}
