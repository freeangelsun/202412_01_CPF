package com.cpf.batch.execution;

import com.cpf.batch.context.CpfBatchContext;
import com.cpf.batch.context.CpfBatchContextBundle;
import com.cpf.batch.context.CpfBatchLaunchMode;
import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import org.springframework.batch.core.job.parameters.JobParameters;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.step.StepExecution;
import org.springframework.batch.infrastructure.item.ExecutionContext;

/** Durable bridge between the trusted Batch control plane and Spring Batch executions. */
public final class CpfBatchExecutionContextSupport {
    static final String TX="cpf.context.transactionId", ROOT_TX="cpf.context.rootTransactionId",
            CORR="cpf.context.correlationId", BUSINESS_DATE="cpf.context.businessDate",
            TX_STARTED="cpf.context.transactionStartedAt", ROOT_EXEC="cpf.context.rootExecutionId",
            PARENT_EXEC="cpf.context.parentExecutionId", PARENT_SEG="cpf.context.parentSegmentId",
            DEADLINE="cpf.context.deadline";
    static final String ORIGINAL_JOB_EXECUTION="cpf.context.originalJobExecutionId",
            LAST_JOB_EXECUTION="cpf.context.lastJobExecutionId",
            RESTART_COUNT="cpf.context.restartCount";
    private final CpfTransactionIdGenerator transactionIds;
    private final CpfExecutionIdGenerator executionIds;
    private final CpfBusinessDateProvider businessDates;

    public CpfBatchExecutionContextSupport(CpfTransactionIdGenerator transactionIds, CpfExecutionIdGenerator executionIds,
                                           CpfBusinessDateProvider businessDates) {
        this.transactionIds=Objects.requireNonNull(transactionIds);
        this.executionIds=Objects.requireNonNull(executionIds);
        this.businessDates=Objects.requireNonNull(businessDates);
    }

    public CpfContextSnapshot launchSnapshot(String standardExecutionId) {
        CpfContext current=CpfContexts.current();
        if(current!=null) return CpfContextSnapshot.capture(current);
        Instant now=Instant.now(); String tx=transactionIds.newTransactionId(); String ex=executionIds.newExecutionId();
        CpfContext root=new CpfContext(
                new CpfContext.CpfTransactionContext(tx,tx,null,null,businessDates.currentBusinessDate(),now,CpfContext.CpfTransactionOriginKind.BATCH,"cpf-batch",null),
                new CpfContext.CpfExecutionContext(standardExecutionId,ex,ex,null,executionIds.newSegmentId(),null,CpfContext.CpfExecutionType.BATCH,1,0,now,null,CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
                null,null,null);
        return CpfContextSnapshot.capture(root);
    }

    public void addDurableParameters(JobParametersBuilder builder, CpfContextSnapshot snapshot) {
        CpfContext e=snapshot.context();
        builder.addString(TX,e.transaction().transactionId(),true)
                .addString(ROOT_TX,e.transaction().rootTransactionId(),true)
                .addString(BUSINESS_DATE,e.transaction().businessDate().toString(),true)
                .addString(TX_STARTED,e.transaction().startedAt().toString(),true)
                .addString(ROOT_EXEC,e.execution().rootExecutionId(),true);
        add(builder,CORR,e.transaction().correlationId());
        add(builder,PARENT_EXEC,e.execution().executionId());
        add(builder,PARENT_SEG,e.execution().segmentId());
        if(e.execution().deadline()!=null) add(builder,DEADLINE,e.execution().deadline().toString());
    }

    public CpfBatchContextBundle restoreStep(JobParameters parameters, StepExecution stepExecution,
                                              int attempt, long fencingToken, String cpfExecutionId,
                                              String jobId, long definitionVersion) {
        LocalDate businessDate=LocalDate.parse(required(parameters,BUSINESS_DATE));
        Instant startedAt=Instant.parse(required(parameters,TX_STARTED));
        Instant deadline=optionalInstant(parameters.getString(DEADLINE));
        String executionId=executionIds.newExecutionId();
        String rootExecution=text(parameters.getString(ROOT_EXEC)); if(rootExecution==null) rootExecution=executionId;
        CpfContext step=new CpfContext(
                new CpfContext.CpfTransactionContext(required(parameters,TX),defaultText(parameters.getString(ROOT_TX),required(parameters,TX)),null,parameters.getString(CORR),businessDate,startedAt,CpfContext.CpfTransactionOriginKind.BATCH,"cpf-batch",null),
                new CpfContext.CpfExecutionContext(stepExecution.getStepName(),executionId,rootExecution,parameters.getString(PARENT_EXEC),executionIds.newSegmentId(),parameters.getString(PARENT_SEG),CpfContext.CpfExecutionType.BATCH,Math.max(1,attempt),1,Instant.now(),deadline,CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),
                null,null,null);
        ExecutionContext ec=stepExecution.getExecutionContext();
        long currentJobExecution=stepExecution.getJobExecutionId();
        ExecutionLineage lineage=restoreExecutionLineage(ec,currentJobExecution);
        long originalJobExecution=lineage.originalJobExecution();
        int restartCount=lineage.restartCount();
        CpfBatchContext batch=new CpfBatchContext(jobId,jobId,(int)Math.min(Integer.MAX_VALUE,definitionVersion),null,
                Long.toString(currentJobExecution),Long.toString(originalJobExecution),stepExecution.getStepName(),Long.toString(stepExecution.getId()),null,null,
                restartCount>0?CpfBatchLaunchMode.RESTART:CpfBatchLaunchMode.MANUAL,businessDate,restartCount,Math.max(1,attempt),null,null,null,null,null,null,null,
                cpfExecutionId+":"+stepExecution.getStepName(),cpfExecutionId,null,null,fencingToken,Instant.now());
        return new CpfBatchContextBundle(CpfContextSnapshot.capture(step),batch);
    }

    /** Spring Batch 6에서 제거된 Step startCount 대신 ExecutionContext에 재시작 계보를 영속화합니다. */
    static ExecutionLineage restoreExecutionLineage(ExecutionContext context,long currentJobExecution) {
        Objects.requireNonNull(context,"context");
        if(currentJobExecution<1) throw new IllegalArgumentException("currentJobExecution must be positive");
        long original=context.containsKey(ORIGINAL_JOB_EXECUTION)
                ? context.getLong(ORIGINAL_JOB_EXECUTION):currentJobExecution;
        if(!context.containsKey(ORIGINAL_JOB_EXECUTION)) context.putLong(ORIGINAL_JOB_EXECUTION,original);
        long last=context.containsKey(LAST_JOB_EXECUTION)
                ? context.getLong(LAST_JOB_EXECUTION):original;
        int restarts=context.containsKey(RESTART_COUNT)
                ? Math.max(0,context.getInt(RESTART_COUNT)):0;
        if(currentJobExecution!=last) {
            restarts=Math.addExact(restarts,1);
            context.putLong(LAST_JOB_EXECUTION,currentJobExecution);
            context.putInt(RESTART_COUNT,restarts);
        } else {
            if(!context.containsKey(LAST_JOB_EXECUTION)) context.putLong(LAST_JOB_EXECUTION,last);
            if(!context.containsKey(RESTART_COUNT)) context.putInt(RESTART_COUNT,restarts);
        }
        return new ExecutionLineage(original,restarts);
    }

    record ExecutionLineage(long originalJobExecution,int restartCount) { }

    private static void add(JobParametersBuilder b,String key,String value){if(text(value)!=null)b.addString(key,value,true);}
    private static String required(JobParameters p,String key){String value=p.getString(key);if(text(value)==null)throw new IllegalStateException(key+" is missing");return value;}
    private static Instant optionalInstant(String value){return text(value)==null?null:Instant.parse(value);}
    private static String text(String value){return value==null||value.isBlank()?null:value.trim();}
    private static String defaultText(String value,String fallback){String normalized=text(value);return normalized==null?fallback:normalized;}
}
