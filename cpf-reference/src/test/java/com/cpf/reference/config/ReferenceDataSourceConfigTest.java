package com.cpf.reference.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ReferenceDataSourceConfigTest {

    @Test
    void createsBatJdbcResourcesFromBatDataSource() {
        DataSource batDataSource = mock(DataSource.class);
        ReferenceDataSourceConfig config = new ReferenceDataSourceConfig();

        JdbcTemplate jdbcTemplate = config.batJdbcTemplate(batDataSource);
        DataSourceTransactionManager transactionManager =
                (DataSourceTransactionManager) config.batTransactionManager(batDataSource);

        assertThat(jdbcTemplate.getDataSource()).isSameAs(batDataSource);
        assertThat(transactionManager.getDataSource()).isSameAs(batDataSource);
    }
}
