package cpf.pfw.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/** PFW 메타 접근 객체가 동일한 PFW DataSource를 공유하는지 검증합니다. */
class PfwDataSourceConfigTest {

    @Test
    void createsPfwMetadataAccessObjects() {
        DataSource dataSource = mock(DataSource.class);
        PfwDataSourceConfig config = new PfwDataSourceConfig();

        JdbcTemplate jdbcTemplate = config.pfwJdbcTemplate(dataSource);
        DataSourceTransactionManager transactionManager =
                (DataSourceTransactionManager) config.pfwTransactionManager(dataSource);

        assertThat(jdbcTemplate.getDataSource()).isSameAs(dataSource);
        assertThat(transactionManager.getDataSource()).isSameAs(dataSource);
    }
}
