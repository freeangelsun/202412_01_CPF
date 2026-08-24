package com.cpf.batch.execution;

import com.cpf.batch.api.BatchExecutionControlPort;
import com.cpf.batch.spi.BatchExecutionLedgerPort;
import com.cpf.batch.spi.BatchFencingPort;
import com.cpf.batch.spi.BatchStepHandler;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.foundation.time.spi.CpfBusinessDateProvider;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
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
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.config.EnableIntegration;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.scheduling.PollerMetadata;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.PollableChannel;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.scheduling.support.PeriodicTrigger;
import org.springframework.transaction.PlatformTransactionManager;

@AutoConfiguration
@ConditionalOnClass(JobOperator.class)
@EnableConfigurationProperties(CpfBatchExecutionProperties.class)
@EnableIntegration
public class CpfBatchExecutionAutoConfiguration {
    @Bean(name = PollerMetadata.DEFAULT_POLLER)
    @ConditionalOnMissingBean(name = PollerMetadata.DEFAULT_POLLER)
    PollerMetadata cpfBatchDefaultPoller(CpfBatchExecutionProperties properties) {
        PollerMetadata poller = new PollerMetadata();
        poller.setTrigger(new PeriodicTrigger(Duration.ofMillis(properties.remotePollIntervalMs())));
        poller.setMaxMessagesPerPoll(properties.maxPartitionCount());
        poller.setReceiveTimeout(Math.min(properties.remotePollIntervalMs(), 1_000L));
        return poller;
    }

    @Bean
    @ConditionalOnMissingBean
    CpfBatchExecutionContextSupport cpfBatchExecutionContextSupport(
            CpfTransactionIdGenerator transactionIds, CpfExecutionIdGenerator executionIds,
            CpfBusinessDateProvider businessDates) {
        return new CpfBatchExecutionContextSupport(transactionIds, executionIds, businessDates);
    }

    @Bean
    @ConditionalOnMissingBean
    CpfBatchStepHandlerRegistry cpfBatchStepHandlerRegistry(List<BatchStepHandler> handlers) {
        return new CpfBatchStepHandlerRegistry(handlers);
    }

    @Bean("cpfBatchTaskExecutor")
    @ConditionalOnMissingBean(name = "cpfBatchTaskExecutor")
    TaskExecutor cpfBatchTaskExecutor(CpfBatchExecutionProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("cpf-batch-");
        executor.setCorePoolSize(properties.executorCoreSize());
        executor.setMaxPoolSize(properties.executorMaxSize());
        executor.setQueueCapacity(properties.executorQueueCapacity());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    @Bean(name = {"cpfBatchRemoteRequests", "cpfBatchRemoteReplies"})
    @ConditionalOnMissingBean(name = {"cpfBatchRemoteRequests", "cpfBatchRemoteReplies"})
    PollableChannel cpfBatchDisabledRemoteChannel() {
        return new CpfDisabledRemoteChannel();
    }

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
    JdbcBatchExecutionControlPlaneAdapter cpfBatchExecutionControlPlaneAdapter(
            JdbcTemplate jdbc, CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        return new JdbcBatchExecutionControlPlaneAdapter(jdbc, sqlCatalogProvider);
    }

    @Bean
    @ConditionalOnMissingBean(com.cpf.batch.spi.BatchApprovedLaunchRequestResolver.class)
    com.cpf.batch.spi.BatchApprovedLaunchRequestResolver cpfBatchApprovedLaunchRequestResolver(
            JdbcTemplate jdbc, ObjectMapper mapper, CpfVendorSqlCatalogProvider sqlCatalogProvider) {
        return new JdbcBatchApprovedLaunchRequestResolver(jdbc, mapper, sqlCatalogProvider);
    }

    @Bean
    CpfBatchExecutionListener cpfBatchExecutionListener(
            BatchExecutionLedgerPort ledger, BatchFencingPort fencing) {
        return new CpfBatchExecutionListener(ledger, fencing);
    }

    @Bean
    CpfBatchDynamicManagerFlowLifecycle cpfBatchDynamicManagerFlowLifecycle(
            BeanFactory beanFactory, IntegrationFlowContext flowContext) {
        return new CpfBatchDynamicManagerFlowLifecycle(beanFactory, flowContext);
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
            CpfBatchExecutionContextSupport contextSupport,
            @Qualifier("cpfBatchRemoteRequests") MessageChannel requests,
            @Qualifier("cpfBatchRemoteReplies") PollableChannel replies,
            @Qualifier("cpfBatchRemoteMessagingTemplate") MessagingTemplate messagingTemplate,
            CpfBatchDynamicManagerFlowLifecycle dynamicManagerFlows) {
        return new CpfBatchJobFactory(repository, transactionManager, taskExecutor, handlers,
                fencing, listener, properties, contextSupport, requests, replies, messagingTemplate,
                dynamicManagerFlows);
    }

    @Bean
    @ConditionalOnMissingBean(BatchExecutionControlPort.class)
    BatchExecutionControlPort cpfSpringBatchExecutionControl(
            JobOperator operator,
            JobRepository repository,
            CpfBatchJobFactory jobs,
            BatchExecutionLedgerPort ledger,
            BatchFencingPort fencing,
            CpfBatchExecutionContextSupport contextSupport) {
        return new CpfSpringBatchExecutionControl(operator, repository, jobs, ledger, fencing, contextSupport);
    }
}
