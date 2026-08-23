package com.cpf.batch.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.configuration.support.JdbcDefaultBatchConfiguration;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.transaction.PlatformTransactionManager;

class BatchRepositoryConfigurationTest {
    @Test
    void usesSpringBatchSixJdbcRepositoryWithCanonicalPlatformTablePrefix() {
        DataSource dataSource = mock(DataSource.class);
        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        BatchRepositoryConfiguration configuration = new BatchRepositoryConfiguration(
                dataSource,
                transactionManager,
                new MockEnvironment().withProperty("cpf.db.vendor", "postgresql"));

        assertThat(configuration).isInstanceOf(JdbcDefaultBatchConfiguration.class);
        assertThat(configuration.getDataSource()).isSameAs(dataSource);
        assertThat(configuration.getTransactionManager()).isSameAs(transactionManager);
        assertThat(configuration.getDatabaseType()).isEqualTo("POSTGRES");
        assertThat(configuration.getTablePrefix()).isEqualTo("BAT_SB_");
    }
}
