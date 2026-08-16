package com.cpf.admin.config;

import com.cpf.data.persistence.api.CpfDataSourceRegistry;
import com.cpf.data.persistence.api.CpfDatabaseRole;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * ADM의 CPF Platform DB 연결을 논리 Database Role로 구성합니다.
 *
 * <p>ADM_* 테이블은 {@link CpfDatabaseRole#CPF_PLATFORM_DB}에 포함됩니다. BAT/Gateway 등
 * 다른 Owner의 테이블을 직접 갱신하지 않고 각 Owner Public API/Operations Port를 사용합니다.</p>
 */
@Configuration
public class AdmJdbcConfig {
    @Bean(name = "admDataSource")
    public DataSource admDataSource(
            CpfDataSourceRegistry dataSources,
            AdmPersistencePolicy persistencePolicy) {
        return persistencePolicy.memoryEnabled()
                ? new AdmMemoryDataSource()
                : dataSources.require(CpfDatabaseRole.CPF_PLATFORM_DB);
    }

    @Bean(name = "admTransactionManager")
    /** admTransactionManager 작업을 CPF 표준 계약에 따라 수행한다. */
    public PlatformTransactionManager admTransactionManager(
            @Qualifier("admDataSource") DataSource admDataSource,
            AdmPersistencePolicy persistencePolicy) {
        return persistencePolicy.memoryEnabled()
                ? new AdmMemoryTransactionManager()
                : new DataSourceTransactionManager(admDataSource);
    }

    @Bean(name = "admJdbcTemplate")
    /** admJdbcTemplate 작업을 CPF 표준 계약에 따라 수행한다. */
    public JdbcTemplate admJdbcTemplate(@Qualifier("admDataSource") DataSource admDataSource) {
        return new JdbcTemplate(admDataSource);
    }
}
