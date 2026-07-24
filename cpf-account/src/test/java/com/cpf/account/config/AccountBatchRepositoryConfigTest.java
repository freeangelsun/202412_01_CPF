package com.cpf.account.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.mock.env.MockEnvironment;

import javax.sql.DataSource;

/** ACC 배치 메타 저장소가 BAT DB와 트랜잭션 관리자를 사용하는지 검증합니다. */
class AccountBatchRepositoryConfigTest {

    @Test
    void usesBatBatchMetadataResources() {
        DataSource batDataSource = mock(DataSource.class);
        PlatformTransactionManager batTransactionManager = mock(PlatformTransactionManager.class);
        TestableConfig config = new TestableConfig(batDataSource, batTransactionManager);

        assertThat(config.dataSource()).isSameAs(batDataSource);
        assertThat(config.transactionManager()).isSameAs(batTransactionManager);
        assertThat(config.databaseType()).isEqualTo("MARIADB");
    }

    private static final class TestableConfig extends AccountBatchRepositoryConfig {
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
