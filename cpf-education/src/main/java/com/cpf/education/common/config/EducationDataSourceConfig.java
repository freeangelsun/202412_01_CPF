package com.cpf.education.common.config;

import com.cpf.data.persistence.api.CpfDataSourceRegistry;
import com.cpf.data.persistence.api.CpfDatabaseRole;
import org.springframework.boot.batch.jdbc.autoconfigure.BatchDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * EDU가 Owner 제공 Database Role을 실제 Consumer로 사용하는 연결 예제입니다.
 *
 * <p>EDU는 Schema/Migration/Seed/DataSource URL을 소유하지 않습니다. CRUD/Search 교육 데이터는
 * {@link CpfDatabaseRole#REFERENCE_FIXTURE}, Spring Batch metadata는
 * {@link CpfDatabaseRole#CPF_PLATFORM_DB}를 Data Provider에서 받아 사용합니다.</p>
 */
@Configuration
public class EducationDataSourceConfig {
    @Bean(name = "educationReferenceFixtureDataSource")
    public DataSource educationReferenceFixtureDataSource(CpfDataSourceRegistry dataSources) {
        return dataSources.require(CpfDatabaseRole.REFERENCE_FIXTURE);
    }

    @Bean(name = "educationReferenceFixtureJdbcTemplate")
    /** educationReferenceFixtureJdbcTemplate 작업을 CPF 표준 계약에 따라 수행한다. */
    public JdbcTemplate educationReferenceFixtureJdbcTemplate(
            @Qualifier("educationReferenceFixtureDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "educationReferenceFixtureTransactionManager")
    public PlatformTransactionManager educationReferenceFixtureTransactionManager(
            @Qualifier("educationReferenceFixtureDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "educationBatchPlatformDataSource")
    @BatchDataSource
    /** educationBatchPlatformDataSource 작업을 CPF 표준 계약에 따라 수행한다. */
    public DataSource educationBatchPlatformDataSource(CpfDataSourceRegistry dataSources) {
        return dataSources.require(CpfDatabaseRole.CPF_PLATFORM_DB);
    }

    @Bean(name = "educationBatchPlatformTransactionManager")
    public PlatformTransactionManager educationBatchPlatformTransactionManager(
            @Qualifier("educationBatchPlatformDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
