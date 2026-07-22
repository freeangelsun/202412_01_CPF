package com.cpf.account.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/** ACC 배치 메타 저장소가 CPF DB와 트랜잭션 관리자를 사용하는지 검증합니다. */
class AccountBatchRepositoryConfigTest {

    @Test
    void usesCpfBatchMetadataResources() {
        DataSource cpfDataSource = mock(DataSource.class);
        PlatformTransactionManager cpfTransactionManager = mock(PlatformTransactionManager.class);
        TestableConfig config = new TestableConfig(cpfDataSource, cpfTransactionManager);

        assertThat(config.dataSource()).isSameAs(cpfDataSource);
        assertThat(config.transactionManager()).isSameAs(cpfTransactionManager);
        assertThat(config.databaseType()).isEqualTo("MYSQL");
    }

    private static final class TestableConfig extends AccountBatchRepositoryConfig {
        private TestableConfig(
                DataSource cpfDataSource,
                PlatformTransactionManager cpfTransactionManager) {
            super(cpfDataSource, cpfTransactionManager);
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
