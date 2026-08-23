package com.cpf.platform.operations.runtimecontrol;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.foundation.execution.api.CpfExecutionCatalogPort;
import com.cpf.foundation.execution.api.CpfOperationAccessPolicy;
import com.cpf.foundation.execution.api.CpfOperationCatalogRegistry;
import com.cpf.platform.operations.channelregistry.application.CpfChannelPolicyService;
import com.cpf.platform.operations.runtimecontrol.catalog.CpfExecutionCatalogRepository;
import java.time.Clock;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/** Operation Catalog/Policy Runtime wiring. */
@AutoConfiguration
@ConditionalOnClass(JdbcTemplate.class)
@ConditionalOnBean(name = "cpfJdbcTemplate")
public class CpfOperationPolicyAutoConfiguration {
    static final String SEED_ALLOWED_CALLERS = "cpf.operation-policy.seed.allowed-callers";
    static final String SEED_SOURCE = "cpf.operation-policy.seed.source";
    static final String SEED_REVISION = "cpf.operation-policy.seed.revision";

    @Bean
    @ConditionalOnMissingBean(CpfOperationAccessPolicy.class)
    CpfJdbcOperationAccessPolicy cpfOperationAccessPolicy(
            @Qualifier("cpfJdbcTemplate") JdbcTemplate jdbc,
            CpfVendorSqlCatalogProvider catalogs,
            Environment env,
            ObjectProvider<Clock> clocks,
            ObjectProvider<CpfChannelPolicyService> channelPolicies) {
        return new CpfJdbcOperationAccessPolicy(
                jdbc,
                catalogs,
                env.getProperty("cpf.operation-policy.refresh-interval", Duration.class, Duration.ofSeconds(2)),
                env.getProperty("cpf.operation-policy.max-stale", Duration.class, Duration.ofMinutes(5)),
                clocks.getIfUnique(Clock::systemUTC),
                channelPolicies.getIfAvailable());
    }

    @Bean(name = "cpfOperationPolicyRuntimeApplier")
    @ConditionalOnMissingBean(name = "cpfOperationPolicyRuntimeApplier")
    CpfRuntimeChangeApplier cpfOperationPolicyRuntimeApplier(CpfOperationAccessPolicy accessPolicy) {
        if (!(accessPolicy instanceof CpfJdbcOperationAccessPolicy jdbcPolicy)) {
            throw new IllegalStateException("Canonical Operation Policy runtime must expose JDBC LKG refresh for hot apply.");
        }
        return new CpfOperationPolicyRuntimeApplier(jdbcPolicy);
    }

    @Bean
    @ConditionalOnMissingBean(CpfOperationCatalogRegistry.class)
    CpfOperationCatalogRegistry cpfOperationCatalogRegistry(
            @Qualifier("cpfJdbcTemplate") JdbcTemplate jdbc,
            @Qualifier("cpfTransactionManager") PlatformTransactionManager manager,
            CpfVendorSqlCatalogProvider catalogs,
            Environment env,
            ObjectProvider<Clock> clocks) {
        String raw = env.getProperty(SEED_ALLOWED_CALLERS, "");
        List<String> callers = raw.isBlank()
                ? List.of()
                : Arrays.stream(raw.split(",")).map(String::trim).filter(v -> !v.isBlank()).toList();
        return new CpfJdbcOperationCatalogRegistry(
                jdbc,
                new TransactionTemplate(manager),
                catalogs,
                clocks.getIfUnique(Clock::systemUTC),
                callers,
                env.getProperty(SEED_SOURCE, "YML"),
                env.getProperty(SEED_REVISION, "UNSPECIFIED"));
    }

    @Bean
    @ConditionalOnBean(CpfVendorSqlCatalogProvider.class)
    @ConditionalOnMissingBean(CpfExecutionCatalogPort.class)
    CpfExecutionCatalogPort cpfExecutionCatalogPort(
            @Qualifier("cpfJdbcTemplate") JdbcTemplate jdbc,
            @Qualifier("cpfTransactionManager") PlatformTransactionManager manager,
            CpfVendorSqlCatalogProvider catalogs) {
        return new CpfExecutionCatalogRepository(jdbc, new TransactionTemplate(manager), catalogs);
    }
}
