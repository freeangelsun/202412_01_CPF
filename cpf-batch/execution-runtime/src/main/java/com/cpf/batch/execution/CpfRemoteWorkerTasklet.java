package com.cpf.batch.execution;

import com.cpf.batch.api.BatchStepDefinition;
import com.cpf.batch.spi.BatchFencingPort;
import org.springframework.batch.core.step.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.infrastructure.repeat.RepeatStatus;

/** Remote Partition/Remote Step Worker가 공유하는 Spring Batch Tasklet입니다. */
public final class CpfRemoteWorkerTasklet implements Tasklet {
    private final CpfBatchStepHandlerRegistry handlers;
    private final BatchFencingPort fencing;
    public CpfRemoteWorkerTasklet(CpfBatchStepHandlerRegistry handlers, BatchFencingPort fencing) {
        this.handlers = handlers; this.fencing = fencing;
    }
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        BatchStepDefinition definition = CpfRemoteStepDefinition.read(
                contribution.getStepExecution().getExecutionContext());
        return new CpfBatchTasklet(definition, handlers, fencing).execute(contribution, chunkContext);
    }
}
