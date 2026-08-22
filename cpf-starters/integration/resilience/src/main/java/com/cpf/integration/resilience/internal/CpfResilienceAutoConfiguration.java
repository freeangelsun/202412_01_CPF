package com.cpf.integration.resilience.internal;

import com.cpf.data.lock.api.CpfLockManager;
import com.cpf.data.lock.api.CpfLockingExecutionGuard;
import com.cpf.platform.operations.observability.api.CpfTelemetry;
import com.cpf.integration.resilience.api.CpfResilienceExecutor;
import com.cpf.integration.resilience.api.CpfResiliencePolicyOperations;
import com.cpf.platform.operations.api.state.CpfStateOperations;
import com.cpf.platform.operations.reconciliation.CpfReconciliationPort;
import com.cpf.platform.operations.reconciliation.CpfReconciliationProbePort;
import com.cpf.platform.operations.reconciliation.CpfReconciliationRuntimePolicy;
import com.cpf.platform.operations.reconciliation.CpfReconciliationWorkPort;
import com.cpf.platform.operations.reconciliation.CpfReconciliationWorker;
import com.cpf.foundation.service.state.DefaultCpfStateOperations;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.platform.operations.spi.state.CpfStateAuditSink;
import com.cpf.platform.operations.spi.state.CpfStateStore;
import com.cpf.data.lock.api.CpfLockManagers;
import com.cpf.data.lock.spi.CpfLockAuditSink;
import com.cpf.data.lock.spi.CpfLockStore;
import com.cpf.integration.resilience.spi.CpfResilienceAuditSink;
import com.cpf.integration.resilience.spi.CpfResilienceFailureClassifier;
import com.cpf.integration.resilience.spi.CpfResiliencePolicyStore;
import com.cpf.integration.resilience.spi.CpfResilienceRuntimePolicyResolver;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.Executors;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * CPF resilience capability의 정책 저장소, 실행 엔진, 분산 상태/잠금 Provider와
 * UNKNOWN 결과 reconcile consumer를 구성하는 자동 구성입니다.
 *
 * <p>{@code cpf.integration.resilience.enabled=true}인 경우에만 활성화되며,
 * JDBC/local provider는 각각의 명시적 속성이 켜진 경우에만 선택됩니다.</p>
 */
@AutoConfiguration
@ConditionalOnProperty(prefix = "cpf.integration.resilience", name = "enabled", havingValue = "true")
public class CpfResilienceAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    Clock cpfResilienceClock() {
        return Clock.systemUTC();
    }

    @Bean
    CpfResiliencePolicyStore cpfResiliencePolicyStore(DataSource dataSource, PlatformTransactionManager transactionManager) {
        return new JdbcCpfResiliencePolicyStore(
                new JdbcTemplate(dataSource), new TransactionTemplate(transactionManager));
    }

    @Bean
    CpfResilienceAuditSink cpfResilienceAuditSink(DataSource dataSource) {
        return new JdbcCpfResilienceAuditSink(new JdbcTemplate(dataSource));
    }

    @Bean
    @ConditionalOnMissingBean
    CpfResilienceFailureClassifier cpfResilienceFailureClassifier() {
        return new CpfDefaultFailureClassifier();
    }

    @Bean
    @ConditionalOnMissingBean
    CpfResilienceRuntimePolicyResolver cpfResilienceRuntimePolicyResolver(Environment environment) {
        return new CpfEnvironmentResilienceRuntimePolicyResolver(environment);
    }

    /**
     * Explicit local-only provider. It is disabled by default so a multi-instance deployment
     * cannot accidentally claim distributed-lock safety from an in-memory map.
     */
    @Bean
    @ConditionalOnMissingBean(CpfLockStore.class)
    @ConditionalOnProperty(prefix = "cpf.integration.resilience.locking", name = "local-provider-enabled",
            havingValue = "true")
    CpfLockStore cpfLocalLockStore(Environment environment, Clock clock) {
        int maximumTrackedKeys = environment.getProperty(
                "cpf.integration.resilience.locking.local.maximum-tracked-keys",
                Integer.class,
                CpfLocalLockStore.DEFAULT_MAXIMUM_TRACKED_KEYS);
        return new CpfLocalLockStore(maximumTrackedKeys, clock);
    }

    @Bean
    @ConditionalOnMissingBean(CpfLockStore.class)
    @ConditionalOnProperty(prefix = "cpf.integration.resilience.locking", name = "jdbc-provider-enabled",
            havingValue = "true")
    CpfLockStore cpfJdbcLockStore(DataSource dataSource, PlatformTransactionManager transactionManager) {
        return new JdbcCpfLockStore(
                new JdbcTemplate(dataSource), new TransactionTemplate(transactionManager));
    }

    /** Shared durable state provider for multi-instance deployments. */
    @Bean
    @ConditionalOnMissingBean(CpfStateStore.class)
    @ConditionalOnProperty(prefix = "cpf.integration.resilience.state", name = "jdbc-provider-enabled",
            havingValue = "true")
    CpfStateStore cpfJdbcStateStore(
            DataSource dataSource,
            PlatformTransactionManager transactionManager,
            Environment environment,
            Clock clock) {
        int maximumStates = environment.getProperty(
                "cpf.integration.resilience.state.jdbc.maximum-states",
                Integer.class,
                JdbcCpfStateStore.DEFAULT_MAXIMUM_STATES);
        int maximumCommands = environment.getProperty(
                "cpf.integration.resilience.state.jdbc.maximum-commands",
                Integer.class,
                JdbcCpfStateStore.DEFAULT_MAXIMUM_COMMANDS);
        Duration commandTtl = environment.getProperty(
                "cpf.integration.resilience.state.jdbc.command-ttl",
                Duration.class,
                JdbcCpfStateStore.DEFAULT_COMMAND_TTL);
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        CpfJdbcStateSchemaVerifier.verify(jdbc);
        return new JdbcCpfStateStore(
                jdbc,
                new TransactionTemplate(transactionManager),
                maximumStates,
                maximumCommands,
                commandTtl,
                clock);
    }

    @Bean
    @ConditionalOnProperty(prefix = "cpf.integration.resilience.state", name = "jdbc-provider-enabled",
            havingValue = "true")
    CpfStateAuditSink cpfJdbcStateAuditSink(DataSource dataSource) {
        return new JdbcCpfStateAuditSink(new JdbcTemplate(dataSource));
    }

    @Bean
    @ConditionalOnBean(CpfStateStore.class)
    @ConditionalOnMissingBean(CpfStateOperations.class)
    CpfStateOperations cpfStateOperations(
            CpfStateStore store,
            ObjectProvider<CpfStateAuditSink> audits,
            Clock clock) {
        List<CpfStateAuditSink> sinks = audits.orderedStream().toList();
        return new DefaultCpfStateOperations(store, clock, sinks);
    }

    @Bean
    @ConditionalOnBean(CpfLockStore.class)
    @ConditionalOnMissingBean
    CpfLockManager cpfLockManager(
            CpfLockStore store, ObjectProvider<CpfLockAuditSink> audit, Clock clock) {
        return CpfLockManagers.create(store, audit.getIfAvailable(), clock);
    }

    @Bean
    @ConditionalOnBean(CpfLockManager.class)
    @ConditionalOnMissingBean
    CpfLockingExecutionGuard cpfLockingExecutionGuard(CpfLockManager manager) {
        return new CpfLockingExecutionGuard(manager);
    }

    @Bean
    CpfResilienceEngine cpfResilienceExecutor(
            CpfResiliencePolicyStore store,
            CpfResilienceFailureClassifier classifier,
            CpfResilienceAuditSink audit,
            CpfResilienceRuntimePolicyResolver runtimePolicyResolver,
            ObjectProvider<CpfLockManager> lockManager,
            ObjectProvider<CpfTelemetry> telemetry,
            CpfContextExecutionFactory contextFactory,
            Clock clock,
            Environment environment) {
        int maximumGuardEntries = environment.getProperty(
                "cpf.integration.resilience.engine.maximum-guard-entries",
                Integer.class,
                10_000);
        Duration guardIdleTtl = environment.getProperty(
                "cpf.integration.resilience.engine.guard-idle-ttl",
                Duration.class,
                Duration.ofMinutes(30));
        if (maximumGuardEntries < 1 || maximumGuardEntries > 1_000_000) {
            throw new IllegalArgumentException(
                    "cpf.integration.resilience.engine.maximum-guard-entries must be between 1 and 1000000");
        }
        if (guardIdleTtl == null || guardIdleTtl.isZero() || guardIdleTtl.isNegative()
                || guardIdleTtl.compareTo(Duration.ofDays(365)) > 0) {
            throw new IllegalArgumentException(
                    "cpf.integration.resilience.engine.guard-idle-ttl must be positive and <= PT8760H");
        }
        CpfTelemetry telemetryProvider = telemetry.getIfAvailable();
        if (telemetryProvider == null) telemetryProvider = CpfTelemetry.noop();
        return new CpfResilienceEngine(store, classifier, audit, runtimePolicyResolver,
                lockManager.getIfAvailable(), telemetryProvider, contextFactory, clock,
                Executors.newVirtualThreadPerTaskExecutor(), Math::random, System::nanoTime,
                maximumGuardEntries, guardIdleTtl);
    }

    /**
     * Activates the real UNKNOWN-result probe consumer when the application supplies durable
     * reconciliation ports. The shared resilience executor is mandatory on this product path.
     */
    @Bean
    @ConditionalOnBean({CpfReconciliationPort.class, CpfReconciliationWorkPort.class,
            CpfReconciliationRuntimePolicy.class})
    @ConditionalOnMissingBean(CpfReconciliationWorker.class)
    CpfReconciliationWorker cpfReconciliationWorker(
            CpfReconciliationPort port,
            CpfReconciliationWorkPort work,
            CpfReconciliationRuntimePolicy policy,
            ObjectProvider<CpfReconciliationProbePort> probes,
            ObjectProvider<CpfLockManager> lockManager,
            ObjectProvider<CpfStateOperations> stateOperations,
            CpfResilienceExecutor resilienceExecutor,
            Clock clock,
            Environment environment) {
        String workerId = environment.getProperty(
                "cpf.reconciliation.worker.id", String.class, "CPF-RECONCILIATION");
        return new CpfReconciliationWorker(
                port, work, policy, probes.orderedStream().toList(), workerId, clock,
                lockManager.getIfAvailable(), stateOperations.getIfAvailable(), resilienceExecutor);
    }

    @Bean
    CpfResiliencePolicyOperations cpfResiliencePolicyOperations(
            CpfResiliencePolicyStore store,
            CpfResilienceAuditSink audit,
            Clock clock,
            PlatformTransactionManager transactionManager) {
        return new CpfResiliencePolicyCommandService(store, audit, clock,
                new CpfSpringResilienceTransactionRunner(new TransactionTemplate(transactionManager)));
    }
}
