package com.cpf.starter.platform.operations.observability;

import com.cpf.core.api.error.CpfResponseCodeResolver;
import com.cpf.platform.operations.api.state.CpfStateOperations;
import com.cpf.platform.operations.observability.api.logging.CpfTransactionSegmentPort;
import com.cpf.platform.operations.observability.internal.logging.CpfBoundaryFailureEvidenceListener;
import com.cpf.platform.operations.observability.internal.logging.CpfTraceSamplingPolicy;
import com.cpf.platform.operations.observability.internal.logging.DynamicTransactionLogLevelService;
import com.cpf.platform.operations.observability.internal.logging.LoggingAspect;
import com.cpf.platform.operations.observability.internal.logging.TransactionLogListener;
import com.cpf.platform.operations.observability.internal.logging.TransactionLogService;
import com.cpf.platform.operations.observability.internal.logging.fallback.CpfTraceRecoveryFacade;
import com.cpf.platform.operations.observability.internal.logging.fallback.TransactionLogFallbackStore;
import com.cpf.platform.operations.observability.internal.logging.fallback.TransactionLogRecoveryWorker;
import com.cpf.platform.operations.observability.internal.logging.fallback.TransactionSegmentFallbackStore;
import com.cpf.platform.operations.observability.internal.logging.fallback.TransactionSegmentRecoveryWorker;
import com.cpf.platform.operations.observability.internal.logging.file.CpfAsyncFileLogWriter;
import com.cpf.platform.operations.observability.internal.logging.file.CpfFileLogWriter;
import com.cpf.platform.operations.observability.internal.logging.file.TransactionFileLogListener;
import com.cpf.platform.operations.observability.internal.logging.policy.JdbcLogPolicyRepository;
import com.cpf.platform.operations.observability.internal.logging.policy.LogPolicyCache;
import com.cpf.platform.operations.observability.internal.logging.policy.LogPolicyRepository;
import com.cpf.platform.operations.observability.internal.logging.policy.LogPolicyResolver;
import com.cpf.platform.operations.observability.internal.logging.segment.CpfTransactionSegmentPortAdapter;
import com.cpf.platform.operations.observability.internal.logging.segment.CpfTransactionTimelineQueryFacade;
import com.cpf.platform.operations.observability.internal.logging.segment.TransactionSegmentPersistenceService;
import com.cpf.platform.operations.observability.internal.logging.segment.TransactionSegmentService;
import com.cpf.platform.operations.observability.spi.logging.CpfTransactionLogPersistencePort;
import com.cpf.platform.operations.observability.spi.logging.lineage.CpfTransactionLineageProjectionPort;
import com.cpf.platform.operations.observability.spi.logging.segment.CpfTransactionSegmentPersistencePort;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Clock;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 실제 CPF 플랫폼 DB Provider가 선택된 Runtime의 거래 원장·구간·복구 조립 Owner입니다.
 *
 * <p>JAR에 구현 클래스가 있다는 이유로 활성화하지 않습니다. 요약 로그, 구간 로그,
 * lineage projection의 세 concrete Port와 플랫폼 TransactionManager가 모두 있을 때만
 * 완전한 Runtime을 만들며 no-op 또는 memory 대체 구현은 만들지 않습니다.</p>
 */
@AutoConfiguration
@ConditionalOnBean(
        value = {
                CpfTransactionLogPersistencePort.class,
                CpfTransactionSegmentPersistencePort.class,
                CpfTransactionLineageProjectionPort.class
        },
        name = "cpfTransactionManager")
public class CpfPersistedTransactionObservabilityAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    DynamicTransactionLogLevelService dynamicTransactionLogLevelService() {
        return new DynamicTransactionLogLevelService();
    }

    @Bean
    @ConditionalOnMissingBean
    CpfFileLogWriter cpfFileLogWriter(Environment environment) {
        return new CpfFileLogWriter(environment);
    }

    @Bean
    @ConditionalOnMissingBean
    CpfAsyncFileLogWriter cpfAsyncFileLogWriter(CpfFileLogWriter writer, Environment environment) {
        return new CpfAsyncFileLogWriter(writer, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    TransactionLogFallbackStore transactionLogFallbackStore(
            @Qualifier("cpfJackson2ObjectMapper") ObjectMapper objectMapper,
            CpfFileLogWriter writer,
            Environment environment) {
        return new TransactionLogFallbackStore(objectMapper, writer, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    TransactionSegmentFallbackStore transactionSegmentFallbackStore(
            @Qualifier("cpfJackson2ObjectMapper") ObjectMapper objectMapper,
            CpfFileLogWriter writer,
            Environment environment) {
        return new TransactionSegmentFallbackStore(objectMapper, writer, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    TransactionLogService transactionLogService(CpfTransactionLogPersistencePort persistence) {
        return new TransactionLogService(persistence);
    }

    @Bean
    @ConditionalOnMissingBean
    TransactionLogListener transactionLogListener(
            TransactionLogService service,
            TransactionLogFallbackStore fallback,
            Environment environment) {
        return new TransactionLogListener(service, fallback, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    TransactionFileLogListener transactionFileLogListener(CpfAsyncFileLogWriter writer) {
        return new TransactionFileLogListener(writer);
    }

    @Bean
    @ConditionalOnMissingBean
    CpfBoundaryFailureEvidenceListener cpfBoundaryFailureEvidenceListener(TransactionLogListener listener) {
        return new CpfBoundaryFailureEvidenceListener(listener);
    }

    // DB Log Policy Runtime 조립 Owner.
    // 이 세 Bean 이 없으면 LoggingAspect 의 정책 평가가 항상 null 이 되어 DB 로그 정책이
    // 조용히 무시되고, ADM 의 log-policy cache refresh/clear 는 항상 400 으로 거절된다.
    // (구현 클래스는 있는데 어디에서도 Bean 으로 만들지 않는 상태였다.)
    // 실제 플랫폼 DB Provider 가 선택된 이 조립에서만 만든다. no-op/in-memory 대체는 만들지 않는다.
    @Bean
    @ConditionalOnMissingBean
    LogPolicyRepository cpfLogPolicyRepository(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplates,
            @Qualifier("cpfDataSource") ObjectProvider<DataSource> dataSources) {
        return new JdbcLogPolicyRepository(jdbcTemplates, dataSources);
    }

    @Bean
    @ConditionalOnMissingBean
    LogPolicyCache cpfLogPolicyCache(
            LogPolicyRepository repository, Environment environment, ObjectProvider<Clock> clocks) {
        return new LogPolicyCache(repository, environment, clocks.getIfUnique(Clock::systemUTC));
    }

    @Bean
    @ConditionalOnMissingBean
    LogPolicyResolver cpfLogPolicyResolver(LogPolicyCache cache) {
        return new LogPolicyResolver(cache);
    }

    @Bean
    @ConditionalOnMissingBean
    LoggingAspect transactionLoggingAspect(
            ApplicationEventPublisher publisher,
            Environment environment,
            DynamicTransactionLogLevelService dynamicLevels,
            ObjectProvider<CpfTraceSamplingPolicy> sampling,
            ObjectProvider<CpfResponseCodeResolver> responseCodes,
            ObjectProvider<LogPolicyResolver> logPolicies,
            CpfTransactionSegmentPort transactionSegments,
            ObjectProvider<Clock> clocks) {
        return new LoggingAspect(
                publisher, environment, dynamicLevels, sampling, responseCodes, logPolicies,
                transactionSegments, clocks);
    }

    @Bean
    @ConditionalOnMissingBean
    TransactionSegmentPersistenceService transactionSegmentPersistenceService(
            CpfTransactionSegmentPersistencePort persistence,
            TransactionSegmentFallbackStore fallback) {
        return new TransactionSegmentPersistenceService(persistence, fallback);
    }

    @Bean
    @ConditionalOnMissingBean
    TransactionSegmentService transactionSegmentService(
            TransactionSegmentPersistenceService persistence,
            ObjectProvider<Clock> clocks) {
        return new TransactionSegmentService(persistence, clocks);
    }

    @Bean
    @ConditionalOnMissingBean
    CpfTransactionSegmentPortAdapter cpfTransactionSegmentPortAdapter(TransactionSegmentService service) {
        return new CpfTransactionSegmentPortAdapter(service);
    }

    @Bean
    @ConditionalOnMissingBean
    TransactionLogRecoveryWorker transactionLogRecoveryWorker(
            TransactionLogFallbackStore fallback,
            TransactionLogService service,
            CpfFileLogWriter writer,
            Environment environment,
            ObjectProvider<CpfStateOperations> stateOperations) {
        return new TransactionLogRecoveryWorker(fallback, service, writer, environment, stateOperations);
    }

    @Bean
    @ConditionalOnMissingBean
    TransactionSegmentRecoveryWorker transactionSegmentRecoveryWorker(
            TransactionSegmentFallbackStore fallback,
            TransactionSegmentPersistenceService persistence,
            CpfFileLogWriter writer,
            Environment environment) {
        return new TransactionSegmentRecoveryWorker(fallback, persistence, writer, environment);
    }

    @Bean
    @ConditionalOnMissingBean
    CpfTraceRecoveryFacade cpfTraceRecoveryFacade(
            TransactionLogRecoveryWorker logs,
            TransactionSegmentRecoveryWorker segments) {
        return new CpfTraceRecoveryFacade(logs, segments);
    }

    @Bean
    @ConditionalOnMissingBean
    CpfTransactionTimelineQueryFacade cpfTransactionTimelineQueryFacade(
            @Qualifier("cpfJdbcTemplate") ObjectProvider<JdbcTemplate> jdbcTemplates) {
        return new CpfTransactionTimelineQueryFacade(jdbcTemplates);
    }
}
