package com.cpf.batch.runtime;

import com.cpf.data.persistence.api.CpfDataSourceRegistry;
import com.cpf.data.persistence.api.CpfDatabaseRole;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * Batch Runtime의 BAT_* persistence를 CPF Platform DB에 연결합니다.
 *
 * <p>별도 batDB를 만들지 않고 {@link CpfDatabaseRole#CPF_PLATFORM_DB}를 소비합니다.
 * Bean 이름은 Batch 내부 Consumer 호환을 위해 유지하되 물리 DB URL/계정은 Data Provider가 소유합니다.</p>
 */
@Configuration
public class BatDataSourceConfiguration {
    @Bean(name = "batDataSource")
    @Primary
    public DataSource batDataSource(CpfDataSourceRegistry dataSources) {
        return dataSources.require(CpfDatabaseRole.CPF_PLATFORM_DB);
    }

    @Bean(name = "batTransactionManager")
    @Primary
    public PlatformTransactionManager batTransactionManager(
            @Qualifier("batDataSource") DataSource batDataSource) {
        return new DataSourceTransactionManager(batDataSource);
    }

    @Bean(name = "batJdbcTemplate")
    @Primary
    public JdbcTemplate batJdbcTemplate(@Qualifier("batDataSource") DataSource batDataSource) {
        return new JdbcTemplate(batDataSource);
    }
}
