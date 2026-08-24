package com.cpf.batch.execution;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.cpf.batch.spi.BatchFencingPort;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.messaging.MessageChannel;
import org.springframework.transaction.PlatformTransactionManager;

class CpfBatchRemoteWorkerConfigurationTest {
    @Test
    void doesNotActivateRemoteFlowsWhenTheSelectedTransportProvidesNoChannels() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getSystemProperties()
                    .put("cpf.batch.remote.worker-enabled", "true");
            context.register(CpfBatchRemoteWorkerConfiguration.class);

            context.refresh();

            assertThat(context.containsBean("cpfBatchRemoteStepWorkerFlow")).isFalse();
            assertThat(context.containsBean("cpfBatchRemoteChunkWorkerFlow")).isFalse();
            assertThat(context.containsBean("cpfRemotePartitionWorkerStep")).isFalse();
        }
    }

    @Test
    void activatesBothWorkerFlowSubscribersBeforeKafkaCanDeliver() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.getEnvironment().getSystemProperties()
                    .put("cpf.batch.remote.worker-enabled", "true");
            context.register(WorkerDependencies.class, CpfBatchRemoteWorkerConfiguration.class);
            context.refresh();

            DirectChannel stepRequests = context.getBean(
                    "cpfBatchWorkerRequests", DirectChannel.class);
            DirectChannel chunkRequests = context.getBean(
                    "cpfBatchChunkWorkerRequests", DirectChannel.class);

            assertThat(stepRequests.getSubscriberCount()).isEqualTo(1);
            assertThat(chunkRequests.getSubscriberCount()).isEqualTo(1);
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class WorkerDependencies {
        @Bean JobRepository jobRepository() { return mock(JobRepository.class); }
        @Bean PlatformTransactionManager transactionManager() {
            return mock(PlatformTransactionManager.class);
        }
        @Bean CpfBatchStepHandlerRegistry handlers() {
            return mock(CpfBatchStepHandlerRegistry.class);
        }
        @Bean BatchFencingPort fencing() { return mock(BatchFencingPort.class); }
        @Bean CpfBatchExecutionContextSupport contextSupport() {
            return mock(CpfBatchExecutionContextSupport.class);
        }
        @Bean CpfBatchExecutionListener listener() {
            return mock(CpfBatchExecutionListener.class);
        }
        @Bean("cpfBatchWorkerRequests") MessageChannel stepRequests() {
            return new DirectChannel();
        }
        @Bean("cpfBatchChunkWorkerRequests") MessageChannel chunkRequests() {
            return new DirectChannel();
        }
        @Bean("cpfBatchWorkerReplies") MessageChannel replies() {
            return new QueueChannel();
        }
    }
}
