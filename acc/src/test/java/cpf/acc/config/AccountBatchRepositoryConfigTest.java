package cpf.acc.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/** ACC 배치 메타 저장소가 PFW DB와 트랜잭션 관리자를 사용하는지 검증합니다. */
class AccountBatchRepositoryConfigTest {

    @Test
    void usesPfwBatchMetadataResources() {
        DataSource pfwDataSource = mock(DataSource.class);
        PlatformTransactionManager pfwTransactionManager = mock(PlatformTransactionManager.class);
        TestableConfig config = new TestableConfig(pfwDataSource, pfwTransactionManager);

        assertThat(config.dataSource()).isSameAs(pfwDataSource);
        assertThat(config.transactionManager()).isSameAs(pfwTransactionManager);
        assertThat(config.databaseType()).isEqualTo("MYSQL");
    }

    private static final class TestableConfig extends AccountBatchRepositoryConfig {
        private TestableConfig(
                DataSource pfwDataSource,
                PlatformTransactionManager pfwTransactionManager) {
            super(pfwDataSource, pfwTransactionManager);
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
