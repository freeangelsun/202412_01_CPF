package com.cpf.member.config;

import com.cpf.core.api.database.CpfDataSources;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
public class MemberDataSourceConfig {

    @Bean
    public DataSource memberDataSource(Environment environment) throws NamingException {
        return CpfDataSources.resolve(environment, "cpf.member.datasource");
    }

    /**
     * Member 저장소가 다른 주제영역 DB를 선택하지 않도록 전용 JDBC 접근 객체를 제공합니다.
     */
    @Bean(name = "memberJdbcTemplate")
    public JdbcTemplate memberJdbcTemplate(
            @Qualifier("memberDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    /**
     * Member 서비스와 배치가 동일한 업무 트랜잭션 경계를 사용하도록 합니다.
     */
    @Bean(name = "memberTransactionManager")
    public PlatformTransactionManager memberTransactionManager(
            @Qualifier("memberDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}