package com.cpf.batch.execution;

import com.cpf.batch.api.BatchStepDefinition;
import com.cpf.batch.spi.BatchFencingPort;
import java.util.Objects;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

/** Remote Partition/Remote Step Worker가 공유하는 Spring Batch Tasklet입니다. */
public final class CpfRemoteWorkerTasklet implements Tasklet {
    private final CpfBatchStepHandlerRegistry handlers;
    private final BatchFencingPort fencing;
    private final CpfBatchExecutionContextSupport contextSupport;
    public CpfRemoteWorkerTasklet(
            CpfBatchStepHandlerRegistry handlers,
            BatchFencingPort fencing,
            CpfBatchExecutionContextSupport contextSupport) {
        this.handlers=Objects.requireNonNull(handlers,"handlers");
        this.fencing=Objects.requireNonNull(fencing,"fencing");
        this.contextSupport=Objects.requireNonNull(contextSupport,"contextSupport");
    }
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        BatchStepDefinition definition = CpfRemoteStepDefinition.read(
                contribution.getStepExecution().getExecutionContext());
        return new CpfBatchTasklet(definition,handlers,fencing,contextSupport).execute(contribution,chunkContext);
    }
}
