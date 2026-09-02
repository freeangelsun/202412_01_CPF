package com.cpf.platform.operations.featureflag.openfeature;

import com.cpf.platform.operations.api.featureflag.CpfFeatureFlagOperations;
import com.cpf.platform.operations.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.platform.operations.spi.featureflag.CpfFeatureFlagAuditSink;
import com.cpf.platform.operations.spi.featureflag.CpfFeatureFlagProvider;
import com.cpf.platform.operations.spi.featureflag.CpfFeatureFlagStateStore;
import java.time.Clock;
import java.time.Duration;
import com.cpf.data.persistence.api.CpfDataSourceRegistry;
import com.cpf.data.persistence.api.CpfDatabaseRole;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

// ADM capability registry(ADMUI-054, canonical_ref ADM-APPROVAL/ADM-AUDIT)가 `featureFlags` 를
// 등록된 ADM 기능으로 선언하고, ADM Route Contract 가 그 operation 들을 요구한다. 즉 이 Port 는
// ADM 을 조립한 Runtime 에서 mandatory 다. 이 모듈을 Composition 에 넣는 행위 자체가 opt-in 이며
// (Generated Domain 은 이 모듈을 포함하지 않는다), 속성은 끄기 위한 수단으로만 남긴다.
// 조건부 opt-in 으로 두면 mandatory Admin Route 가 설정으로 사라진다.
@AutoConfiguration
@ConditionalOnProperty(
        prefix = "cpf.platform-operations.feature-flag",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
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

    // 무자격 DataSource/TransactionManager/Clock 주입은 1-WAS 처럼 후보가 여럿인 합성에서 기동을
    // 막는다. 형제 Platform 구성과 동일하게 canonical role/Bean 이름으로 해소한다.
    @Bean
    CpfFeatureFlagStateStore cpfFeatureFlagStateStore(
            CpfDataSourceRegistry dataSources,
            @Qualifier("cpfCommonTransactionManager") PlatformTransactionManager transactionManager) {
        return new JdbcCpfFeatureFlagStateStore(
                new JdbcTemplate(dataSources.require(CpfDatabaseRole.CPF_PLATFORM_DB)),
                new TransactionTemplate(transactionManager));
    }

    @Bean
    CpfFeatureFlagAuditSink cpfFeatureFlagAuditSink(CpfDataSourceRegistry dataSources) {
        return new JdbcCpfFeatureFlagAuditSink(
                new JdbcTemplate(dataSources.require(CpfDatabaseRole.CPF_PLATFORM_DB)));
    }

    @Bean
    CpfFeatureFlagOperations cpfFeatureFlagOperations(
            CpfFeatureFlagProvider provider,
            CpfFeatureFlagStateStore stateStore,
            CpfFeatureFlagAuditSink auditSink,
            @Qualifier("cpfStarterClock") Clock clock,
            @Qualifier("cpfCommonTransactionManager") PlatformTransactionManager transactionManager) {
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
