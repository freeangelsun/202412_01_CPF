package com.cpf.bizadmin.config;

import com.cpf.data.persistence.api.CpfDataSourceRegistry;
import com.cpf.data.persistence.api.CpfDatabaseRole;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * BZA의 Customer Business Admin DB 연결을 논리 Database Role로 구성합니다.
 *
 * <p>BZA는 CPF Platform DB와 분리된 {@link CpfDatabaseRole#BZA_DB}를 소비합니다.
 * URL/계정/Secret은 Data Provider가 소유하고 BZA Source에 물리 DB 이름을 고정하지 않습니다.</p>
 */
@Configuration
@ConditionalOnProperty(prefix = "cpf.bza.datasource", name = "enabled", havingValue = "true")
public class BzaDataSourceConfig {

    /** BZA 업무 관리자 DB datasource입니다. */
    @Bean(name = "bzaDataSource")
    public DataSource bzaDataSource(CpfDataSourceRegistry dataSources) {
        return dataSources.require(CpfDatabaseRole.BZA_DB);
    }

    /** BZA SQL 실행용 named parameter JDBC template입니다. */
    @Bean(name = "bzaJdbcTemplate")
    public NamedParameterJdbcTemplate bzaJdbcTemplate(@Qualifier("bzaDataSource") DataSource bzaDataSource) {
        return new NamedParameterJdbcTemplate(bzaDataSource);
    }

    /** BZA DB transaction manager입니다. */
    @Bean(name = "bzaTransactionManager")
    public PlatformTransactionManager bzaTransactionManager(@Qualifier("bzaDataSource") DataSource bzaDataSource) {
        return new DataSourceTransactionManager(bzaDataSource);
    }
}
