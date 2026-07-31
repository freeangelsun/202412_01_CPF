package com.cpf.batch.execution;

import com.cpf.batch.api.BatchExecutionControlPort;
import com.cpf.batch.spi.BatchExecutionLedgerPort;
import com.cpf.batch.spi.BatchFencingPort;
import com.cpf.batch.spi.BatchStepHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.integration.channel.QueueChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@AutoConfiguration
@ConditionalOnClass(JobOperator.class)
@EnableConfigurationProperties(CpfBatchExecutionProperties.class)
public class CpfBatchExecutionAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    CpfBatchStepHandlerRegistry cpfBatchStepHandlerRegistry(List<BatchStepHandler> handlers) {
        return new CpfBatchStepHandlerRegistry(handlers);
    }

    @Bean("cpfBatchTaskExecutor")
    @ConditionalOnMissingBean(name = "cpfBatchTaskExecutor")
    TaskExecutor cpfBatchTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("cpf-batch-");
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(32);
        executor.setQueueCapacity(0);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    @Bean("cpfBatchRemoteRequests")
    @ConditionalOnMissingBean(name = "cpfBatchRemoteRequests")
    MessageChannel cpfBatchRemoteRequests() { return new PublishSubscribeChannel(); }

    @Bean("cpfBatchRemoteReplies")
    @ConditionalOnMissingBean(name = "cpfBatchRemoteReplies")
    PollableChannel cpfBatchRemoteReplies() { return new QueueChannel(); }

    @Bean("cpfBatchRemoteMessagingTemplate")
    @ConditionalOnMissingBean(name = "cpfBatchRemoteMessagingTemplate")
    MessagingTemplate cpfBatchRemoteMessagingTemplate(
            @Qualifier("cpfBatchRemoteRequests") MessageChannel requests) {
        MessagingTemplate template = new MessagingTemplate();
        template.setDefaultChannel(requests);
        template.setSendTimeout(30_000L);
        template.setReceiveTimeout(30_000L);
        return template;
    }

    @Bean
    @ConditionalOnMissingBean({BatchExecutionLedgerPort.class, BatchFencingPort.class})
    JdbcBatchExecutionControlPlaneAdapter cpfBatchExecutionControlPlaneAdapter(JdbcTemplate jdbc) {
        return new JdbcBatchExecutionControlPlaneAdapter(jdbc);
    }

    @Bean
    @ConditionalOnMissingBean(com.cpf.batch.spi.BatchApprovedLaunchRequestResolver.class)
    com.cpf.batch.spi.BatchApprovedLaunchRequestResolver cpfBatchApprovedLaunchRequestResolver(
            JdbcTemplate jdbc, ObjectMapper mapper) {
        return new JdbcBatchApprovedLaunchRequestResolver(jdbc, mapper);
    }

    @Bean
    CpfBatchExecutionListener cpfBatchExecutionListener(
            BatchExecutionLedgerPort ledger, BatchFencingPort fencing) {
        return new CpfBatchExecutionListener(ledger, fencing);
    }

    @Bean
    CpfBatchJobFactory cpfBatchJobFactory(
            JobRepository repository,
            PlatformTransactionManager transactionManager,
            @Qualifier("cpfBatchTaskExecutor") TaskExecutor taskExecutor,
            CpfBatchStepHandlerRegistry handlers,
            BatchFencingPort fencing,
            CpfBatchExecutionListener listener,
            CpfBatchExecutionProperties properties,
            @Qualifier("cpfBatchRemoteRequests") MessageChannel requests,
            @Qualifier("cpfBatchRemoteReplies") PollableChannel replies,
            @Qualifier("cpfBatchRemoteMessagingTemplate") MessagingTemplate messagingTemplate) {
        return new CpfBatchJobFactory(repository, transactionManager, taskExecutor, handlers,
                fencing, listener, properties, requests, replies, messagingTemplate);
    }

    @Bean
    @ConditionalOnMissingBean(BatchExecutionControlPort.class)
    BatchExecutionControlPort cpfSpringBatchExecutionControl(
            JobOperator operator,
            JobRepository repository,
            CpfBatchJobFactory jobs,
            BatchExecutionLedgerPort ledger,
            BatchFencingPort fencing) {
        return new CpfSpringBatchExecutionControl(operator, repository, jobs, ledger, fencing);
    }
}
