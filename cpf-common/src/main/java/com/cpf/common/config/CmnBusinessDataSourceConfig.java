package com.cpf.common.config;

import com.cpf.core.common.database.CpfDataSourceResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * CMN 업무 공통 DB(cmnDB)를 선택적으로 연결하는 설정입니다.
 *
 * <p>CPF 코드/메시지/응답코드 캐시는 기존 CPF datasource를 사용하고, 채번/알림/공통 업무 로그처럼
 * 여러 업무 모듈이 공유하는 비즈니스 공통 데이터는 별도 datasource를 사용합니다.</p>
 */
@Configuration
@ConditionalOnProperty(prefix = "cpf.cmn.business-db", name = "enabled", havingValue = "true")
public class CmnBusinessDataSourceConfig {

    /** 활성화된 프로젝트에서 URL 또는 JNDI 방식으로 cmnDB 연결을 생성합니다. */
    @Bean(name = "cmnBusinessDataSource")
    public DataSource cmnBusinessDataSource(Environment environment) throws NamingException {
        return CpfDataSourceResolver.resolve(environment, "spring.datasource.cmn-business");
    }

    @Bean(name = "cmnBusinessJdbcTemplate")
    public JdbcTemplate cmnBusinessJdbcTemplate(@Qualifier("cmnBusinessDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "cmnBusinessTransactionManager")
    public PlatformTransactionManager cmnBusinessTransactionManager(
            @Qualifier("cmnBusinessDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "cmnBusinessTransactionTemplate")
    public TransactionTemplate cmnBusinessTransactionTemplate(
            @Qualifier("cmnBusinessTransactionManager") PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
