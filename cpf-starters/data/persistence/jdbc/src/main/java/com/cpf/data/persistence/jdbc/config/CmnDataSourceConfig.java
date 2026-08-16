package com.cpf.data.persistence.jdbc.config;

import com.cpf.data.persistence.jdbc.CpfDataSources;
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
    @Bean(name = "cpfCommonDataSource")
    public DataSource cpfCommonDataSource(Environment environment) throws NamingException {
        return CpfDataSources.resolve(environment, "spring.datasource.cmn");
    }
    @Bean(name = "cpfCommonTransactionManager")
    /** cpfCommonTransactionManager 작업을 CPF 표준 계약에 따라 수행한다. */
    public PlatformTransactionManager cpfCommonTransactionManager(@Qualifier("cpfCommonDataSource") DataSource cpfCommonDataSource) {
        return new DataSourceTransactionManager(cpfCommonDataSource);
    }
}
