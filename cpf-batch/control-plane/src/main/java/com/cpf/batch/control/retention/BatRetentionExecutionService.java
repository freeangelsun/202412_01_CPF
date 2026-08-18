package com.cpf.batch.control.retention;

import com.cpf.platform.operations.api.retention.CpfRetentionCommand;
import com.cpf.platform.operations.api.retention.CpfRetentionPolicy;
import com.cpf.platform.operations.api.retention.CpfRetentionResult;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
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
        this.runtimeInstanceId=resolveRuntimeInstanceId(environment);
    }

    public List<BatRetentionPolicyDefinition> policies(){ return repository.findPolicies(); }
    public List<BatRetentionRunSnapshot> runs(String policyId,int limit){ return repository.findRuns(policyId,Math.max(1,Math.min(limit,500))); }
    public BatRetentionPolicyDefinition savePolicy(BatRetentionPolicyDefinition policy,String actor){
        Instant next=policy.nextRunAt()!=null?policy.nextRunAt():nextRun(policy.scheduleExpression(),clock.instant());
        return repository.savePolicy(new BatRetentionPolicyDefinition(policy.policyId(),policy.target(),policy.action(),policy.retentionDays(),policy.scheduleExpression(),policy.maintenanceStart(),policy.maintenanceEnd(),policy.enabled(),policy.legalHold(),policy.chunkSize(),policy.throttleMillis(),policy.maxRowsPerRun(),policy.maxRuntimeSeconds(),policy.leaseSeconds(),policy.policyVersion(),next,policy.rowVersion()),actor);
    }

    /**
     * Manual destructive execution always performs a server-side dry-run first.
     * The ADM preview is an operator UX safeguard; this check makes the API boundary safe even when called directly.
     */
    public BatRetentionRunSnapshot runNow(String policyId,String actor,String reason){
        BatRetentionPolicyDefinition p=repository.findPolicy(policyId).orElseThrow(()->new IllegalArgumentException("Retention policy 없음: "+policyId));
        Instant now=clock.instant();
        Instant cutoff=now.minus(Duration.ofDays(p.retentionDays()));
        executeChunkWithRetry(new CpfRetentionCommand(new CpfRetentionPolicy(p.target(),p.action(),p.legalHold(),true),cutoff,actor,reason,Math.max(1,p.chunkSize())));
        return startNew(p,"MANUAL",actor,reason,now,cutoff);
    }
    public BatRetentionRunSnapshot runScheduled(String policyId){ return startNew(policyId,"SCHEDULED","CPF_RETENTION_SCHEDULER","scheduled retention"); }
    public BatRetentionRunSnapshot requestPause(String runId,String actor){ repository.requestPause(runId,actor); return repository.findRun(runId).orElseThrow(); }
    public BatRetentionPolicyDefinition pausePolicy(String policyId,boolean paused,String actor){ repository.setPolicyPaused(policyId,paused,actor); return repository.findPolicy(policyId).orElseThrow(); }

    public BatRetentionRunSnapshot resume(String runId,String actor,String reason){
        BatRetentionRunSnapshot run=repository.findRun(runId).orElseThrow(()->new IllegalArgumentException("Retention run 없음: "+runId));
        if(!"PAUSED".equals(run.status()) && !"PARTIAL".equals(run.status()) && !"FAILED".equals(run.status())) throw new IllegalStateException("재개할 수 없는 Run 상태: "+run.status());
        BatRetentionPolicyDefinition p=repository.findPolicy(run.policyId()).orElseThrow();
        Instant now=clock.instant();
        if(!repository.claim(p.policyId(),runtimeInstanceId,now,leaseUntil(p,now))) throw new IllegalStateException("RETENTION_LEASE_BUSY");
        repository.markRunning(runId,runtimeInstanceId,actor);
        try { executeLoop(runId,p,run.cutoffAt(),actor,reason,run); }
        finally { repository.release(p.policyId(),runtimeInstanceId,nextRun(p.scheduleExpression(),clock.instant())); }
        return repository.findRun(runId).orElseThrow();
    }

    private BatRetentionRunSnapshot startNew(String policyId,String trigger,String actor,String reason){
        BatRetentionPolicyDefinition p=repository.findPolicy(policyId).orElseThrow(()->new IllegalArgumentException("Retention policy 없음: "+policyId));
        Instant now=clock.instant();
        return startNew(p,trigger,actor,reason,now,now.minus(Duration.ofDays(p.retentionDays())));
    }

    private BatRetentionRunSnapshot startNew(BatRetentionPolicyDefinition p,String trigger,String actor,String reason,Instant now,Instant cutoff){
        if(!p.enabled()) throw new IllegalStateException("RETENTION_POLICY_DISABLED");
        if(!inMaintenanceWindow(p,now)) throw new IllegalStateException("RETENTION_OUTSIDE_MAINTENANCE_WINDOW");
        if(!repository.claim(p.policyId(),runtimeInstanceId,now,leaseUntil(p,now))) throw new IllegalStateException("RETENTION_LEASE_BUSY");
        String runId=UUID.randomUUID().toString();
        BatRetentionRunSnapshot run=new BatRetentionRunSnapshot(runId,p.policyId(),trigger,"RUNNING",runtimeInstanceId,actor,reason,p.policyVersion(),cutoff,now,null,0,0,0,0,0,0,false,null,null);
        repository.createRun(run);
        try { executeLoop(runId,p,cutoff,actor,reason,run); }
        finally { repository.release(p.policyId(),runtimeInstanceId,nextRun(p.scheduleExpression(),clock.instant())); }
        return repository.findRun(runId).orElseThrow();
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

    private static String resolveRuntimeInstanceId(Environment e){
        String explicit=e.getProperty("cpf.runtime.instance-id"); if(explicit!=null&&!explicit.isBlank())return explicit.trim();
        try{return InetAddress.getLocalHost().getHostName();}catch(Exception ignored){String host=System.getenv("HOSTNAME");if(host!=null&&!host.isBlank())return host;throw new IllegalStateException("Runtime hostname을 확인할 수 없습니다.");}
    }
}
