package cpf.acc.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/** ACC 저장소가 전용 DataSource를 사용하는지 검증합니다. */
class AccountDataSourceConfigTest {

    @Test
    void createsDedicatedJdbcTemplate() {
        DataSource dataSource = mock(DataSource.class);

        JdbcTemplate jdbcTemplate = new AccountDataSourceConfig().accJdbcTemplate(dataSource);
        DataSourceTransactionManager transactionManager =
                (DataSourceTransactionManager) new AccountDataSourceConfig().accTransactionManager(dataSource);

        assertThat(jdbcTemplate.getDataSource()).isSameAs(dataSource);
        assertThat(transactionManager.getDataSource()).isSameAs(dataSource);
    }
}
