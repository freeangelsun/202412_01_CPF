package com.cpf.education.common.config;
import org.junit.jupiter.api.Test;
import org.springframework.boot.batch.jdbc.autoconfigure.BatchDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import java.lang.reflect.Method;
import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EducationDataSourceConfigTest {

    @Test
    void createsBatJdbcResourcesFromBatDataSource() {
        DataSource educationBatchPlatformDataSource = mock(DataSource.class);
        EducationDataSourceConfig config = new EducationDataSourceConfig();

        JdbcTemplate jdbcTemplate = config.batJdbcTemplate(educationBatchPlatformDataSource);
        DataSourceTransactionManager transactionManager =
                (DataSourceTransactionManager) config.educationBatchPlatformTransactionManager(educationBatchPlatformDataSource);

        assertThat(jdbcTemplate.getDataSource()).isSameAs(educationBatchPlatformDataSource);
        assertThat(transactionManager.getDataSource()).isSameAs(educationBatchPlatformDataSource);
    }

    @Test
    void marksBatDataSourceAsSpringBatchOwner() throws NoSuchMethodException {
        Method factoryMethod = EducationDataSourceConfig.class.getMethod(
                "educationBatchPlatformDataSource",
                org.springframework.core.env.Environment.class);

        assertThat(factoryMethod.getAnnotation(BatchDataSource.class)).isNotNull();
    }
}
