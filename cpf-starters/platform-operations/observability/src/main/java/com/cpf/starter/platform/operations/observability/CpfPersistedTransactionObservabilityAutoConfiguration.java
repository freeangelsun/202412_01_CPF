package com.cpf.starter.platform.operations.observability;

import com.cpf.core.api.error.CpfMessageResolver;
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

    @Bean
    @ConditionalOnMissingBean
    LoggingAspect transactionLoggingAspect(
            ApplicationEventPublisher publisher,
            Environment environment,
            DynamicTransactionLogLevelService dynamicLevels,
            ObjectProvider<CpfTraceSamplingPolicy> sampling,
            ObjectProvider<CpfMessageResolver> messages,
            ObjectProvider<CpfResponseCodeResolver> responseCodes,
            ObjectProvider<LogPolicyResolver> logPolicies,
            CpfTransactionSegmentPort transactionSegments,
            ObjectProvider<Clock> clocks) {
        return new LoggingAspect(
                publisher, environment, dynamicLevels, sampling, messages, responseCodes, logPolicies,
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
