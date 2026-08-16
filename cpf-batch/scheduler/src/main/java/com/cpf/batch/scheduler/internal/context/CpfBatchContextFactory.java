package com.cpf.batch.scheduler.internal.context;

import com.cpf.batch.context.CpfBatchContext;
import com.cpf.batch.context.CpfBatchContextBundle;
import com.cpf.batch.context.CpfBatchLaunchMode;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/** Batch 경계가 Core Context와 Batch metadata를 명시적으로 조립하는 Owner factory입니다. */
public final class CpfBatchContextFactory {
    private final CpfTransactionIdGenerator transactionIds;
    private final CpfExecutionIdGenerator executionIds;
    private final CpfBusinessDateProvider businessDates;
    public CpfBatchContextFactory(CpfTransactionIdGenerator transactionIds, CpfExecutionIdGenerator executionIds,
                                  CpfBusinessDateProvider businessDates) {
        this.transactionIds=Objects.requireNonNull(transactionIds);
        this.executionIds=Objects.requireNonNull(executionIds);
        this.businessDates=Objects.requireNonNull(businessDates);
    }
    public CpfBatchContextBundle newSchedulerRoot(String jobName, String scheduleId, LocalDate businessDate,
            String standardExecutionId, String triggerId, Instant deadline) {
        LocalDate date=businessDate==null?businessDates.currentBusinessDate():businessDate;
        Instant now=Instant.now(); String tx=transactionIds.newTransactionId(); String ex=executionIds.newExecutionId();
        String sg=executionIds.newSegmentId();
        String idem="BATCH:"+required(jobName,"jobName")+":"+required(scheduleId,"scheduleId")+":"+date;
        var operation=new CpfContext.CpfOperationContext(jobName,standardExecutionId,triggerId,idem,
                CpfContext.CpfIdempotencyScope.TRANSACTION,CpfContext.CpfIdempotencyMode.REQUIRED,null,null);
        var context=new CpfContext(
                new CpfContext.CpfTransactionContext(tx,tx,null,null,date,now,CpfContext.CpfTransactionOriginKind.SCHEDULE,"cpf-batch-scheduler",null),
                new CpfContext.CpfExecutionContext(standardExecutionId,ex,ex,null,sg,null,CpfContext.CpfExecutionType.SCHEDULED,1,0,now,deadline,CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
                operation,null,null);
        var batch=new CpfBatchContext(jobName,jobName,0,null,null,null,null,null,scheduleId,triggerId,
                CpfBatchLaunchMode.SCHEDULED,date,0,1,null,null,null,null,null,null,null,null,null,null,null,0L,now);
        return new CpfBatchContextBundle(CpfContextSnapshot.capture(context),batch);
    }
    public CpfBatchContextBundle restart(CpfBatchContextBundle original, String jobInstanceId, String jobExecutionId,
            String originalJobExecutionId, int restartCount, String recoveryId, long fencingToken) {
        Objects.requireNonNull(original); int attempt=Math.max(1,original.batch().attempt()+1);
        CpfContext child=child(original.snapshot().context(),CpfContext.CpfExecutionType.BATCH,attempt,original.snapshot().execution().deadline(),original.snapshot().operation());
        return new CpfBatchContextBundle(CpfContextSnapshot.capture(child),
                original.batch().withJobExecution(jobInstanceId,jobExecutionId,restartCount,attempt,originalJobExecutionId,recoveryId,fencingToken));
    }
    public CpfBatchContextBundle childStep(CpfBatchContextBundle parent, String stepName, String stepExecutionId,
            String partitionId, String workerId, String checkpointId, int attempt, long fencingToken) {
        CpfBatchContext b=parent.batch(); int nextAttempt=Math.max(1,attempt);
        CpfContext child=child(parent.snapshot().context(),CpfContext.CpfExecutionType.BATCH,nextAttempt,parent.snapshot().execution().deadline(),parent.snapshot().operation());
        var next=new CpfBatchContext(b.jobName(),b.jobDisplayName(),b.jobVersion(),b.jobInstanceId(),b.jobExecutionId(),b.originalJobExecutionId(),
                stepName,stepExecutionId,b.scheduleId(),b.triggerId(),b.launchMode(),b.businessDate(),b.restartCount(),nextAttempt,partitionId,b.shardKey(),
                b.remoteRequestId(),b.remoteReplyId(),workerId,b.workerGroup(),b.itemId(),checkpointId,b.processStateId(),b.recoveryId(),b.unknownOutcomeId(),fencingToken,Instant.now());
        return new CpfBatchContextBundle(CpfContextSnapshot.capture(child),next,parent.centerCut());
    }
    public CpfBatchContextBundle unknown(CpfBatchContextBundle original, String unknownOutcomeId, String recoveryId, int attempt) {
        CpfBatchContext b=original.batch(); int nextAttempt=Math.max(1,attempt);
        CpfContext child=child(original.snapshot().context(),CpfContext.CpfExecutionType.INTERNAL,nextAttempt,original.snapshot().execution().deadline(),original.snapshot().operation());
        var next=new CpfBatchContext(b.jobName(),b.jobDisplayName(),b.jobVersion(),b.jobInstanceId(),b.jobExecutionId(),b.originalJobExecutionId(),
                b.stepName(),b.stepExecutionId(),b.scheduleId(),b.triggerId(),b.launchMode(),b.businessDate(),b.restartCount(),nextAttempt,b.partitionId(),b.shardKey(),
                b.remoteRequestId(),b.remoteReplyId(),b.workerId(),b.workerGroup(),b.itemId(),b.checkpointId(),b.processStateId(),recoveryId,unknownOutcomeId,b.fencingToken(),Instant.now());
        return new CpfBatchContextBundle(CpfContextSnapshot.capture(child),next,original.centerCut());
    }
    private CpfContext child(CpfContext parent, CpfContext.CpfExecutionType type, int attempt, Instant deadline, CpfContext.CpfOperationContext operation) {
        var p=parent.execution(); Instant now=Instant.now();
        var e=new CpfContext.CpfExecutionContext(p.standardExecutionId(),executionIds.newExecutionId(),p.rootExecutionId(),p.executionId(),executionIds.newSegmentId(),p.segmentId(),type,attempt,p.callDepth()+1,now,deadline,p.cancellationMode());
        return new CpfContext(parent.transaction(),e,operation,parent.identity(),parent.tenant());
    }
    private static String required(String v,String n){if(v==null||v.isBlank())throw new IllegalArgumentException(n+" is required");return v.trim();}
}
