package com.cpf.batch.execution;

import com.cpf.batch.spi.BatchFencingPort;
import java.util.Map;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.Step;
import org.springframework.batch.core.step.StepLocator;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.integration.chunk.RemoteChunkingWorkerBuilder;
import org.springframework.batch.integration.partition.StepExecutionRequestHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.PlatformTransactionManager;

/** Spring Batch 표준 Remote Partition/Chunk/Step Worker 실행 경로입니다. */
@AutoConfiguration(after = CpfBatchKafkaRemoteConfiguration.class)
@ConditionalOnProperty(name = "cpf.batch.remote.worker-enabled", havingValue = "true")
public class CpfBatchRemoteWorkerConfiguration {
    @Bean("cpfRemotePartitionWorkerStep")
    Step cpfRemotePartitionWorkerStep(
            JobRepository repository,
            PlatformTransactionManager transactionManager,
            CpfBatchStepHandlerRegistry handlers,
            BatchFencingPort fencing,
            CpfBatchExecutionListener listener) {
        return new StepBuilder("cpfRemotePartitionWorkerStep", repository)
                .tasklet(new CpfRemoteWorkerTasklet(handlers, fencing), transactionManager)
                .listener(listener)
                .build();
    }

    @Bean("cpfRemoteStepWorker")
    Step cpfRemoteStepWorker(
            JobRepository repository,
            PlatformTransactionManager transactionManager,
            CpfBatchStepHandlerRegistry handlers,
            BatchFencingPort fencing,
            CpfBatchExecutionListener listener) {
        return new StepBuilder("cpfRemoteStepWorker", repository)
                .tasklet(new CpfRemoteWorkerTasklet(handlers, fencing), transactionManager)
                .listener(listener)
                .build();
    }

    @Bean
    StepExecutionRequestHandler cpfBatchRemoteStepExecutionRequestHandler(
            JobRepository repository,
            @Qualifier("cpfRemotePartitionWorkerStep") Step partitionStep,
            @Qualifier("cpfRemoteStepWorker") Step remoteStep) {
        Map<String, Step> steps = Map.of(partitionStep.getName(), partitionStep, remoteStep.getName(), remoteStep);
        StepLocator locator = steps::get;
        StepExecutionRequestHandler handler = new StepExecutionRequestHandler();
        handler.setJobRepository(repository);
        handler.setStepLocator(locator);
        return handler;
    }

    @Bean
    IntegrationFlow cpfBatchRemoteStepWorkerFlow(
            @Qualifier("cpfBatchWorkerRequests") MessageChannel input,
            @Qualifier("cpfBatchWorkerReplies") MessageChannel output,
            StepExecutionRequestHandler handler) {
        return IntegrationFlow.from(input)
                .handle(handler, "handle")
                .channel(output)
                .get();
    }

    @Bean
    IntegrationFlow cpfBatchRemoteChunkWorkerFlow(
            @Qualifier("cpfBatchChunkWorkerRequests") MessageChannel input,
            @Qualifier("cpfBatchWorkerReplies") MessageChannel output,
            CpfBatchStepHandlerRegistry handlers,
            BatchFencingPort fencing) {
        return new RemoteChunkingWorkerBuilder<Map<String, Object>, Map<String, Object>>()
                .inputChannel(input)
                .outputChannel(output)
                .itemProcessor(new CpfRemoteChunkItemProcessor())
                .itemWriter(new CpfRemoteChunkItemWriter(handlers, fencing))
                .build();
    }
}
