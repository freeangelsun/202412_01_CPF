package com.cpf.admin.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class AdmJdbcConfigTest {

    @Test
    void createsOnlyAdmOwnedJdbcResources() {
        DataSource admDataSource = mock(DataSource.class);
        AdmJdbcConfig config = new AdmJdbcConfig();

        JdbcTemplate jdbcTemplate = config.admJdbcTemplate(admDataSource);
        DataSourceTransactionManager transactionManager =
                (DataSourceTransactionManager) config.admTransactionManager(admDataSource);

        assertThat(jdbcTemplate.getDataSource()).isSameAs(admDataSource);
        assertThat(transactionManager.getDataSource()).isSameAs(admDataSource);
    }
}
