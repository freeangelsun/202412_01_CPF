package com.cpf.common.config;

import com.cpf.core.api.database.CpfDataSources;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.naming.NamingException;
import javax.sql.DataSource;

/** 정식 CPF Product mode의 필수 CMN 데이터소스를 구성합니다. */
@Configuration
@ConditionalOnExpression("'${cpf.common.runtime-mode:product}'.toLowerCase() == 'product'")
public class CmnDataSourceConfig {
    @Bean(name = "cmnDataSource")
    public DataSource cmnDataSource(Environment environment) throws NamingException {
        return CpfDataSources.resolve(environment, "spring.datasource.cmn");
    }
    @Bean(name = "cmnTransactionManager")
    public PlatformTransactionManager cmnTransactionManager(@Qualifier("cmnDataSource") DataSource cmnDataSource) {
        return new DataSourceTransactionManager(cmnDataSource);
    }
}
