package com.cpf.data.persistence.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.cpf.data.persistence.api.database.CpfNamedParameterJdbcOperations;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.transaction.PlatformTransactionManager;

class CpfJdbcStarterAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CpfJdbcStarterAutoConfiguration.class))
            .withPropertyValues("cpf.data.persistence.jdbc.enabled=false");

    @Test
    void createsOperationsForOneDataSourceAndOneTransactionManager() {
        runner.withBean("dataSource", DataSource.class, () -> mock(DataSource.class))
                .withBean("transactionManager", PlatformTransactionManager.class,
                        () -> mock(PlatformTransactionManager.class))
                .run(context -> assertThat(context)
                        .hasSingleBean(CpfNamedParameterJdbcOperations.class));
    }

    @Test
    void doesNotGuessWhenDataSourceCandidatesAreAmbiguous() {
        runner.withBean("firstDataSource", DataSource.class, () -> mock(DataSource.class))
                .withBean("secondDataSource", DataSource.class, () -> mock(DataSource.class))
                .withBean("transactionManager", PlatformTransactionManager.class,
                        () -> mock(PlatformTransactionManager.class))
                .run(context -> assertThat(context)
                        .doesNotHaveBean(CpfNamedParameterJdbcOperations.class));
    }

    @Test
    void doesNotGuessWhenTransactionManagerCandidatesAreAmbiguous() {
        runner.withBean("dataSource", DataSource.class, () -> mock(DataSource.class))
                .withBean("firstTransactionManager", PlatformTransactionManager.class,
                        () -> mock(PlatformTransactionManager.class))
                .withBean("secondTransactionManager", PlatformTransactionManager.class,
                        () -> mock(PlatformTransactionManager.class))
                .run(context -> assertThat(context)
                        .doesNotHaveBean(CpfNamedParameterJdbcOperations.class));
    }
}
