package com.cpf.platform.operations.runtimecontrol;

import com.cpf.data.persistence.api.database.CpfVendorSqlCatalog;
import com.cpf.data.persistence.api.database.CpfVendorSqlCatalogProvider;
import com.cpf.foundation.execution.api.CpfExecutionCatalogPort;
import com.cpf.foundation.execution.api.CpfOperationAccessPolicy;
import com.cpf.foundation.execution.api.CpfOperationCatalogRegistry;
import com.cpf.platform.operations.runtimecontrol.catalog.CpfExecutionCatalogRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CpfOperationPolicyAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CpfOperationPolicyAutoConfiguration.class))
            .withBean("cpfJdbcTemplate", JdbcTemplate.class, () -> mock(JdbcTemplate.class))
            .withBean("cpfTransactionManager", PlatformTransactionManager.class,
                    () -> mock(PlatformTransactionManager.class))
            .withBean(CpfVendorSqlCatalogProvider.class, () -> {
                CpfVendorSqlCatalogProvider provider = mock(CpfVendorSqlCatalogProvider.class);
                when(provider.forModule("cpf")).thenReturn(mock(CpfVendorSqlCatalog.class));
                return provider;
            })
            .withBean(CpfOperationAccessPolicy.class, () -> mock(CpfOperationAccessPolicy.class))
            .withBean("cpfOperationPolicyRuntimeApplier", CpfRuntimeChangeApplier.class,
                    () -> mock(CpfRuntimeChangeApplier.class))
            .withBean(ObjectMapper.class, ObjectMapper::new);

    @Test
    void createsRealCatalogProviderFromCanonicalPlatformPersistenceAliases() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(CpfOperationCatalogRegistry.class);
            assertThat(context.getBean(CpfOperationCatalogRegistry.class))
                    .isInstanceOf(CpfJdbcOperationCatalogRegistry.class);
            assertThat(context).hasSingleBean(CpfExecutionCatalogPort.class);
            assertThat(context.getBean(CpfExecutionCatalogPort.class))
                    .isInstanceOf(CpfExecutionCatalogRepository.class);
        });
    }
}
