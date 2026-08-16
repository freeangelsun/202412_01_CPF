package com.cpf.platform.operations.featureflag.openfeature;

import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagOperations;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.spi.featureflag.CpfFeatureFlagAuditSink;
import com.cpf.platform.operations.spi.featureflag.CpfFeatureFlagProvider;
import com.cpf.platform.operations.spi.featureflag.CpfFeatureFlagStateStore;
import java.time.Clock;
import java.time.Duration;
import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

@AutoConfiguration
@ConditionalOnProperty(
        prefix = "cpf.platform-operations.feature-flag",
        name = "enabled",
        havingValue = "true")
public class CpfFeatureFlagAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    Clock cpfFeatureFlagClock() {
        return Clock.systemUTC();
    }

    @Bean
    @ConditionalOnMissingBean
    CpfFeatureFlagProvider cpfOpenFeatureProvider() {
        return new CpfOpenFeatureProviderAdapter("cpf", 0);
    }

    @Bean
    CpfFeatureFlagStateStore cpfFeatureFlagStateStore(
            DataSource dataSource,
            PlatformTransactionManager transactionManager) {
        return new JdbcCpfFeatureFlagStateStore(
                new JdbcTemplate(dataSource),
                new TransactionTemplate(transactionManager));
    }

    @Bean
    CpfFeatureFlagAuditSink cpfFeatureFlagAuditSink(DataSource dataSource) {
        return new JdbcCpfFeatureFlagAuditSink(new JdbcTemplate(dataSource));
    }

    @Bean
    CpfFeatureFlagOperations cpfFeatureFlagOperations(
            CpfFeatureFlagProvider provider,
            CpfFeatureFlagStateStore stateStore,
            CpfFeatureFlagAuditSink auditSink,
            Clock clock,
            PlatformTransactionManager transactionManager) {
        return new CpfFeatureFlagRuntime(
                provider,
                stateStore,
                auditSink,
                clock,
                Duration.ofSeconds(30),
                new CpfSpringFeatureFlagTransactionRunner(
                        new TransactionTemplate(transactionManager)));
    }

    /** Runtime Control Agent가 Feature Flag kill-switch를 실제 hot-apply하도록 연결합니다. */
    @Bean(name = "cpfFeatureFlagRuntimeChangeApplier")
    @ConditionalOnMissingBean(name = "cpfFeatureFlagRuntimeChangeApplier")
    CpfRuntimeChangeApplier cpfFeatureFlagRuntimeChangeApplier(
            CpfFeatureFlagOperations operations) {
        return new CpfFeatureFlagRuntimeChangeApplier(operations);
    }
}
