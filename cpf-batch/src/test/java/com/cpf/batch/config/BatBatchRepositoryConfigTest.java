package com.cpf.batch.config;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.mock.env.MockEnvironment;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class BatBatchRepositoryConfigTest {

    @Test
    void usesBatMetadataResources() {
        DataSource batDataSource = mock(DataSource.class);
        PlatformTransactionManager batTransactionManager = mock(PlatformTransactionManager.class);
        TestableConfig config = new TestableConfig(batDataSource, batTransactionManager);

        assertThat(config.dataSource()).isSameAs(batDataSource);
        assertThat(config.transactionManager()).isSameAs(batTransactionManager);
        assertThat(config.databaseType()).isEqualTo("MARIADB");
    }

    private static final class TestableConfig extends BatBatchRepositoryConfig {
        private TestableConfig(
                DataSource batDataSource,
                PlatformTransactionManager batTransactionManager) {
            super(batDataSource, batTransactionManager, new MockEnvironment());
        }

        private DataSource dataSource() {
            return getDataSource();
        }

        private PlatformTransactionManager transactionManager() {
            return getTransactionManager();
        }

        private String databaseType() {
            return getDatabaseType();
        }
    }
}
