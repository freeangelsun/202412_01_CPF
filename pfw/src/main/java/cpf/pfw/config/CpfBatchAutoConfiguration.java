package cpf.pfw.config;

import cpf.pfw.common.batch.CpfBatchEventPublisher;
import cpf.pfw.common.batch.CpfBatchGhostDetectionService;
import cpf.pfw.common.batch.CpfBatchHeartbeatService;
import cpf.pfw.common.batch.CpfBatchLauncher;
import cpf.pfw.common.batch.CpfBatchLockManager;
import cpf.pfw.common.batch.CpfBatchLoggingEventPublisher;
import cpf.pfw.common.batch.CpfBatchOperationRepository;
import cpf.pfw.common.batch.CpfBatchRuntimeListener;
import cpf.pfw.common.logging.policy.LogPolicyResolver;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.Map;

/**
 * CPF 배치 공통 API 자동 구성입니다.
 *
 * <p>Spring Batch를 실제로 사용하는 모듈에서만 활성화됩니다. ACC/MBR처럼 배치를 쓰지 않는 모듈에는
 * Spring Batch 런타임 의존성을 전파하지 않아 다중 datasource 자동설정 충돌을 막습니다.</p>
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "org.springframework.batch.core.launch.JobLauncher")
public class CpfBatchAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CpfBatchEventPublisher cpfBatchEventPublisher() {
        return new CpfBatchLoggingEventPublisher();
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfBatchOperationRepository cpfBatchOperationRepository(
            @Qualifier("pfwJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            @Qualifier("pfwDataSource") ObjectProvider<DataSource> dataSourceProvider) {
        return new CpfBatchOperationRepository(jdbcTemplateProvider, dataSourceProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfBatchLockManager cpfBatchLockManager(
            @Qualifier("pfwJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplateProvider,
            @Qualifier("pfwDataSource") ObjectProvider<DataSource> dataSourceProvider) {
        return new CpfBatchLockManager(jdbcTemplateProvider, dataSourceProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfBatchHeartbeatService cpfBatchHeartbeatService(
            CpfBatchOperationRepository repository,
            @Value("${cpf.batch.worker.heartbeat-interval-seconds:5}") int heartbeatIntervalSeconds,
            @Value("${cpf.batch.worker.heartbeat-timeout-seconds:30}") int heartbeatTimeoutSeconds) {
        return new CpfBatchHeartbeatService(repository, heartbeatIntervalSeconds, heartbeatTimeoutSeconds);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfBatchRuntimeListener cpfBatchRuntimeListener(
            CpfBatchHeartbeatService heartbeatService,
            ObjectProvider<LogPolicyResolver> logPolicyResolverProvider) {
        return new CpfBatchRuntimeListener(heartbeatService, logPolicyResolverProvider);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfBatchGhostDetectionService cpfBatchGhostDetectionService(
            CpfBatchOperationRepository repository,
            CpfBatchHeartbeatService heartbeatService) {
        return new CpfBatchGhostDetectionService(repository, heartbeatService);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfBatchLauncher cpfBatchLauncher(
            ObjectProvider<JobLauncher> jobLauncherProvider,
            ObjectProvider<JobExplorer> jobExplorerProvider,
            ObjectProvider<JobOperator> jobOperatorProvider,
            ObjectProvider<Map<String, Job>> jobsProvider,
            ObjectProvider<CpfBatchEventPublisher> eventPublisherProvider,
            CpfBatchOperationRepository repository,
            CpfBatchLockManager lockManager,
            @Value("${cpf.batch.lock-ttl-seconds:600}") int lockTtlSeconds) {
        return new CpfBatchLauncher(
                jobLauncherProvider,
                jobExplorerProvider,
                jobOperatorProvider,
                jobsProvider,
                eventPublisherProvider,
                repository,
                lockManager,
                lockTtlSeconds);
    }
}
