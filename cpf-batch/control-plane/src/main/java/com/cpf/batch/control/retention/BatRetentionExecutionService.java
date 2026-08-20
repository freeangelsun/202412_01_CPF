package com.cpf.batch.control.retention;

import com.cpf.platform.operations.api.retention.CpfRetentionCommand;
import com.cpf.platform.operations.api.retention.CpfRetentionPolicy;
import com.cpf.platform.operations.api.retention.CpfRetentionResult;
import com.cpf.foundation.runtime.CpfInstanceIdentity;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.List;
import java.util.UUID;

/**
 * Scheduled/Manual/Resume가 공유하는 단일 retention execution engine.
 * Handler 한 번 = 한 DB transaction/chunk이며, pause/limit은 chunk 경계에서만 적용합니다.
 */
@Service
public class BatRetentionExecutionService {
    private final BatRetentionOperations operations;
    private final BatRetentionExecutionRepository repository;
    private final Environment environment;
    private final Clock clock;
    private final String runtimeInstanceId;

    public BatRetentionExecutionService(BatRetentionOperations operations, BatRetentionExecutionRepository repository,
                                        Environment environment) {
        this(operations, repository, environment, Clock.systemUTC());
    }
    BatRetentionExecutionService(BatRetentionOperations operations, BatRetentionExecutionRepository repository,
                                 Environment environment, Clock clock) {
        this.operations=operations; this.repository=repository; this.environment=environment; this.clock=clock;
        this.runtimeInstanceId=CpfInstanceIdentity.instanceId();
    }

    public List<BatRetentionPolicyDefinition> policies(){ return repository.findPolicies(); }
    public List<BatRetentionRunSnapshot> runs(String policyId,int limit){ return repository.findRuns(policyId,Math.max(1,Math.min(limit,500))); }
    public BatRetentionPolicyDefinition savePolicy(BatRetentionPolicyDefinition policy,String requestedBy,String approvedBy,
                                                     String approvalRequestId,String reason){
        requiredApproval(requestedBy, approvedBy, approvalRequestId, reason);
        Instant next=policy.nextRunAt()!=null?policy.nextRunAt():nextRun(policy.scheduleExpression(),clock.instant());
        BatRetentionPolicyDefinition saved = repository.savePolicy(new BatRetentionPolicyDefinition(policy.policyId(),policy.target(),policy.action(),policy.retentionDays(),policy.scheduleExpression(),policy.maintenanceStart(),policy.maintenanceEnd(),policy.enabled(),policy.legalHold(),policy.chunkSize(),policy.throttleMillis(),policy.maxRowsPerRun(),policy.maxRuntimeSeconds(),policy.leaseSeconds(),policy.policyVersion(),next,policy.rowVersion()),requestedBy);
        repository.audit("POLICY_SAVE","POLICY",policy.policyId(),requestedBy,approvedBy,approvalRequestId,reason,
                policy.rowVersion(),"SUCCEEDED");
        return saved;
    }

    /**
     * Manual destructive execution always performs a server-side dry-run first.
     * The ADM preview is an operator UX safeguard; this check makes the API boundary safe even when called directly.
     */
    public BatRetentionRunSnapshot runNow(String policyId,long expectedVersion,String requestedBy,String approvedBy,
                                           String approvalRequestId,String reason){
        requiredApproval(requestedBy, approvedBy, approvalRequestId, reason);
        BatRetentionPolicyDefinition p=policyAtVersion(policyId, expectedVersion);
        Instant now=clock.instant();
        Instant cutoff=now.minus(Duration.ofDays(p.retentionDays()));
        executeChunkWithRetry(new CpfRetentionCommand(new CpfRetentionPolicy(p.target(),p.action(),p.legalHold(),true),cutoff,requestedBy,reason,Math.max(1,p.chunkSize())));
        repository.audit("RUN_NOW","POLICY",policyId,requestedBy,approvedBy,approvalRequestId,reason,expectedVersion,"STARTED");
        BatRetentionRunSnapshot run = startNew(p,"MANUAL",requestedBy,reason,now,cutoff,expectedVersion);
        repository.audit("RUN_NOW","POLICY",policyId,requestedBy,approvedBy,approvalRequestId,reason,expectedVersion,auditState(run));
        return run;
    }
    public BatRetentionRunSnapshot runScheduled(String policyId){ return startNew(policyId,"SCHEDULED","CPF_RETENTION_SCHEDULER","scheduled retention"); }
    public BatRetentionRunSnapshot requestPause(String runId,long expectedVersion,String actor,String reason){
        BatRetentionRunSnapshot run=repository.findRun(runId)
                .orElseThrow(()->new IllegalArgumentException("Retention run 없음: "+runId));
        policyAtVersion(run.policyId(), expectedVersion);
        repository.requestPause(runId,actor,reason);
        repository.audit("RUN_PAUSE","RUN",runId,actor,null,null,reason,expectedVersion,"SUCCEEDED");
        return repository.findRun(runId).orElseThrow();
    }
    public BatRetentionPolicyDefinition pausePolicy(String policyId,boolean paused,long expectedVersion,String requestedBy,
                                                     String reason,String approvedBy,String approvalRequestId){
        if(!paused) requiredApproval(requestedBy,approvedBy,approvalRequestId,reason);
        repository.setPolicyPaused(policyId,paused,requestedBy,expectedVersion);
        repository.audit(paused?"POLICY_PAUSE":"POLICY_RESUME","POLICY",policyId,requestedBy,approvedBy,
                approvalRequestId,reason,expectedVersion,"SUCCEEDED");
        return repository.findPolicy(policyId).orElseThrow();
    }

    public BatRetentionRunSnapshot resume(String runId,long expectedVersion,String requestedBy,String approvedBy,
                                           String approvalRequestId,String reason){
        requiredApproval(requestedBy, approvedBy, approvalRequestId, reason);
        BatRetentionRunSnapshot run=repository.findRun(runId).orElseThrow(()->new IllegalArgumentException("Retention run 없음: "+runId));
        if(!"PAUSED".equals(run.status()) && !"PARTIAL".equals(run.status()) && !"FAILED".equals(run.status())) throw new IllegalStateException("재개할 수 없는 Run 상태: "+run.status());
        BatRetentionPolicyDefinition p=policyAtVersion(run.policyId(), expectedVersion);
        Instant now=clock.instant();
        if(!repository.claim(p.policyId(),runtimeInstanceId,now,leaseUntil(p,now),expectedVersion)) throw new IllegalStateException("RETENTION_VERSION_OR_LEASE_CONFLICT");
        repository.markRunning(runId,runtimeInstanceId,requestedBy);
        try { executeLoop(runId,p,run.cutoffAt(),requestedBy,reason,run); }
        finally { repository.release(p.policyId(),runtimeInstanceId,nextRun(p.scheduleExpression(),clock.instant())); }
        BatRetentionRunSnapshot finalRun=repository.findRun(runId).orElseThrow();
        repository.audit("RUN_RESUME","RUN",runId,requestedBy,approvedBy,approvalRequestId,reason,expectedVersion,auditState(finalRun));
        return finalRun;
    }

    public List<java.util.Map<String,Object>> audits(String approvalRequestId) {
        return repository.findAuditsByApprovalRequestId(approvalRequestId);
    }

    private static String auditState(BatRetentionRunSnapshot run) {
        if (run == null || run.status() == null) return "UNKNOWN";
        return switch (run.status().trim().toUpperCase(java.util.Locale.ROOT)) {
            case "SUCCESS", "COMPLETED" -> "SUCCEEDED";
            case "FAILED", "ERROR" -> "FAILED";
            case "PAUSED", "PARTIAL", "RUNNING" -> "PENDING";
            default -> "UNKNOWN";
        };
    }

    private BatRetentionRunSnapshot startNew(String policyId,String trigger,String actor,String reason){
        BatRetentionPolicyDefinition p=repository.findPolicy(policyId).orElseThrow(()->new IllegalArgumentException("Retention policy 없음: "+policyId));
        Instant now=clock.instant();
        return startNew(p,trigger,actor,reason,now,now.minus(Duration.ofDays(p.retentionDays())),null);
    }

    private BatRetentionRunSnapshot startNew(BatRetentionPolicyDefinition p,String trigger,String actor,String reason,Instant now,Instant cutoff,Long expectedVersion){
        if(!p.enabled()) throw new IllegalStateException("RETENTION_POLICY_DISABLED");
        if(!inMaintenanceWindow(p,now)) throw new IllegalStateException("RETENTION_OUTSIDE_MAINTENANCE_WINDOW");
        if(!repository.claim(p.policyId(),runtimeInstanceId,now,leaseUntil(p,now),expectedVersion)) throw new IllegalStateException("RETENTION_VERSION_OR_LEASE_CONFLICT");
        String runId=UUID.randomUUID().toString();
        BatRetentionRunSnapshot run=new BatRetentionRunSnapshot(runId,p.policyId(),trigger,"RUNNING",runtimeInstanceId,actor,reason,p.policyVersion(),cutoff,now,null,0,0,0,0,0,0,false,null,null);
        repository.createRun(run);
        try { executeLoop(runId,p,cutoff,actor,reason,run); }
        finally { repository.release(p.policyId(),runtimeInstanceId,nextRun(p.scheduleExpression(),clock.instant())); }
        return repository.findRun(runId).orElseThrow();
    }

    private BatRetentionPolicyDefinition policyAtVersion(String policyId,long expectedVersion){
        if(expectedVersion<0) throw new IllegalArgumentException("expectedVersion은 0 이상이어야 합니다.");
        BatRetentionPolicyDefinition policy=repository.findPolicy(policyId)
                .orElseThrow(()->new IllegalArgumentException("Retention policy 없음: "+policyId));
        if(policy.rowVersion()!=expectedVersion) throw new IllegalStateException("RETENTION_POLICY_VERSION_CONFLICT");
        return policy;
    }

    private static void requiredApproval(String requestedBy,String approvedBy,String approvalRequestId,String reason){
        if(requestedBy==null||requestedBy.isBlank()) throw new IllegalArgumentException("requestedBy is required");
        if(approvedBy==null||approvedBy.isBlank()) throw new IllegalArgumentException("approvedBy is required");
        if(approvalRequestId==null||approvalRequestId.isBlank()) throw new IllegalArgumentException("approvalRequestId is required");
        if(requestedBy.trim().equals(approvedBy.trim())) throw new IllegalArgumentException("requester and approver must differ");
        if(reason==null||reason.isBlank()) throw new IllegalArgumentException("reason is required");
    }

    private void executeLoop(String runId,BatRetentionPolicyDefinition p,Instant cutoff,String actor,String reason,BatRetentionRunSnapshot initial){
        long matched=initial.matchedCount(),archived=initial.archivedCount(),deleted=initial.deletedCount(),processed=initial.processedCount(),compressed=initial.compressedCount(),freed=initial.freedBytes();
        long started=System.nanoTime();
        try {
            while(true){
                if(repository.pauseRequested(runId)){repository.finish(runId,"PAUSED",null,null);return;}
                long elapsed=Duration.ofNanos(System.nanoTime()-started).toSeconds();
                if(processed>=p.maxRowsPerRun() || elapsed>=p.maxRuntimeSeconds()){repository.finish(runId,"PARTIAL",null,"run limit reached");return;}
                Instant leaseNow=clock.instant();
                if(!repository.renewLease(p.policyId(),runtimeInstanceId,leaseNow,leaseUntil(p,leaseNow))) {
                    throw new IllegalStateException("RETENTION_LEASE_LOST");
                }
                int limit=(int)Math.min(p.chunkSize(),p.maxRowsPerRun()-processed);
                CpfRetentionResult r=executeChunkWithRetry(new CpfRetentionCommand(new CpfRetentionPolicy(p.target(),p.action(),p.legalHold(),false),cutoff,actor,reason,limit));
                matched=Math.max(matched,r.matched()); archived+=r.archived(); deleted+=r.purged(); processed+=r.processed(); compressed+=0; freed+=r.freedBytes();
                repository.progress(runId,matched,archived,deleted,processed,compressed,freed);
                Instant afterChunk=clock.instant();
                if(!repository.renewLease(p.policyId(),runtimeInstanceId,afterChunk,leaseUntil(p,afterChunk))) {
                    throw new IllegalStateException("RETENTION_LEASE_LOST");
                }
                if(r.legalHold()){repository.finish(runId,"SKIPPED","LEGAL_HOLD","legal hold enabled");return;}
                if(r.processed()==0 || !r.hasMore()){repository.finish(runId,"SUCCESS",null,null);return;}
                if(repository.pauseRequested(runId)){repository.finish(runId,"PAUSED",null,null);return;}
                if(p.throttleMillis()>0) sleep(p.throttleMillis());
            }
        } catch(RuntimeException failure){
            repository.finish(runId,"FAILED",failure.getClass().getSimpleName(),failure.getMessage());
        }
    }

    private CpfRetentionResult executeChunkWithRetry(CpfRetentionCommand c){
        int retries=Math.max(0,Math.min(environment.getProperty("cpf.retention.retry.max-attempts",Integer.class,3),10));
        long backoff=Math.max(10,environment.getProperty("cpf.retention.retry.backoff-ms",Long.class,250L));
        RuntimeException last=null;
        for(int attempt=0;attempt<=retries;attempt++){
            try{return operations.execute(c);}catch(RuntimeException ex){last=ex;if(attempt==retries)break;sleep(Math.min(backoff*(1L<<Math.min(attempt,6)),10_000L));}
        }
        throw last==null?new IllegalStateException("RETENTION_CHUNK_FAILED"):last;
    }
    private static void sleep(long ms){try{Thread.sleep(ms);}catch(InterruptedException e){Thread.currentThread().interrupt();throw new IllegalStateException("RETENTION_INTERRUPTED",e);}}
    private boolean inMaintenanceWindow(BatRetentionPolicyDefinition p,Instant instant){
        if(p.maintenanceStart()==null || p.maintenanceEnd()==null) return true;
        LocalTime now=instant.atZone(ZoneOffset.UTC).toLocalTime();
        if(p.maintenanceStart().equals(p.maintenanceEnd())) return true;
        if(p.maintenanceStart().isBefore(p.maintenanceEnd())) return !now.isBefore(p.maintenanceStart()) && now.isBefore(p.maintenanceEnd());
        return !now.isBefore(p.maintenanceStart()) || now.isBefore(p.maintenanceEnd());
    }
    static Instant nextRun(String cron,Instant now){
        if(cron==null||cron.isBlank()) return now.plus(Duration.ofDays(1));
        try { var next=CronExpression.parse(cron).next(now.atZone(ZoneOffset.UTC)); return next==null?now.plus(Duration.ofDays(1)):next.toInstant(); }
        catch(IllegalArgumentException ex){ throw new IllegalArgumentException("잘못된 retention schedule: "+cron,ex); }
    }
    /**
     * The lease must cover the configured run window even if one chunk blocks longer than the nominal lease.
     * Per-chunk renewals keep the lease fresh; this floor prevents a second WAS from taking over mid-chunk.
     */
    private static Instant leaseUntil(BatRetentionPolicyDefinition p, Instant now) {
        long safetySeconds=Math.min(86_400L, Math.max((long)p.leaseSeconds(), p.maxRuntimeSeconds()+30L));
        return now.plusSeconds(safetySeconds);
    }


}
