package com.cpf.backoffice.online.config;

import com.cpf.data.persistence.api.CpfDataSourceRegistry;
import com.cpf.data.persistence.api.CpfDatabaseRole;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * MBW의 Backoffice-owned Customer Business DB를 Generated Domain과 동일한 논리 DB Role로 연결합니다.
 * 다른 Business Domain DB는 이 설정으로 등록하거나 직접 접근하지 않습니다.
 */
@Configuration
public class BackofficeDataSourceConfig {
    @Bean(name = "MBW_DATA_SOURCE")
    DataSource mbwDataSource(CpfDataSourceRegistry dataSources) {
        return dataSources.require(CpfDatabaseRole.CUSTOMER_BUSINESS_DB);
    }

    @Bean(name = "MBW_JDBC_TEMPLATE")
    NamedParameterJdbcTemplate mbwJdbcTemplate(@Qualifier("MBW_DATA_SOURCE") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean(name = "MBW_TRANSACTION_MANAGER")
    PlatformTransactionManager mbwTransactionManager(@Qualifier("MBW_DATA_SOURCE") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
