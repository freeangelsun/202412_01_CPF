package com.cpf.account.config;

import com.cpf.core.common.database.CpfDataSourceResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * embedded URL 연결과 external Tomcat JNDI 연결을 동일한 모듈에서 선택합니다.
 */
@Configuration
public class AccountDataSourceConfig {

    @Bean
    @Primary
    public DataSource accDataSource(Environment environment) throws NamingException {
        return CpfDataSourceResolver.resolve(environment, "cpf.datasource");
    }

    /**
     * ACC 저장소가 다른 주제영역 DataSource와 섞이지 않도록 전용 JDBC 접근 객체를 제공합니다.
     *
     * @param dataSource ACC 소유 DataSource
     * @return ACC 테이블 전용 JDBC 접근 객체
     */
    @Bean(name = "accJdbcTemplate")
    @Primary
    public JdbcTemplate accJdbcTemplate(@Qualifier("accDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * ACC 서비스 계층과 Spring Batch가 ACC DB의 트랜잭션 경계를 일관되게 사용하도록 합니다.
     *
     * @param dataSource ACC 소유 DataSource
     * @return ACC 기본 트랜잭션 관리자
     */
    @Bean(name = "accTransactionManager")
    @Primary
    public PlatformTransactionManager accTransactionManager(
            @Qualifier("accDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
