package com.cpf.admin.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AdmJdbcConfigTest {

    @Test
    void createsBatJdbcResourcesFromBatDataSource() {
        DataSource batDataSource = mock(DataSource.class);
        AdmJdbcConfig config = new AdmJdbcConfig();

        JdbcTemplate jdbcTemplate = config.batJdbcTemplate(batDataSource);
        DataSourceTransactionManager transactionManager =
                (DataSourceTransactionManager) config.batTransactionManager(batDataSource);

        assertThat(jdbcTemplate.getDataSource()).isSameAs(batDataSource);
        assertThat(transactionManager.getDataSource()).isSameAs(batDataSource);
    }
}
