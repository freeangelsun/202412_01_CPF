package com.cpf.core.config;

import com.cpf.core.common.database.CpfDataSourceResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiTemplate;
import org.springframework.transaction.PlatformTransactionManager;

import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * CPF 프레임워크 메타 DB 연결을 구성합니다.
 *
 * <p>거래 로그, 거래 메타, 로그 정책, 캐시 이벤트, 배치 운영 메타처럼 프레임워크가
 * 직접 소유하는 데이터는 cpfDB에 저장합니다.</p>
 */
@Configuration
public class CpfDataSourceConfig {

    /**
     * 외부 WAS JNDI와 embedded URL 모드가 같은 조회 계약을 사용하도록 JNDI 접근 객체를 제공합니다.
     */
    @Bean(name = "cpfJndiTemplate")
    public JndiTemplate cpfJndiTemplate() {
        return new JndiTemplate();
    }

    @Bean(name = "cpfDataSource")
    public DataSource cpfDataSource(
            Environment environment,
            @Qualifier("cpfJndiTemplate") JndiTemplate jndiTemplate) throws NamingException {
        return CpfDataSourceResolver.resolve(environment, "spring.datasource.cpf", jndiTemplate);
    }

    @Bean(name = "cpfTransactionManager")
    public PlatformTransactionManager cpfTransactionManager(@Qualifier("cpfDataSource") DataSource cpfDataSource) {
        return new DataSourceTransactionManager(cpfDataSource);
    }

    /**
     * 모든 실행 모듈이 CPF 소유 메타를 같은 이름으로 접근하도록 공통 JDBC 객체를 제공합니다.
     *
     * <p>표준 실행 카탈로그, 거래 메타, 배치 관제, 서비스 레지스트리처럼 CPF가 소유하는
     * 저장소는 업무 모듈이 별도 객체를 중복 선언하지 않고 이 bean을 사용합니다.</p>
     *
     * @param cpfDataSource CPF 메타 DB 연결
     * @return CPF 메타 전용 JDBC 접근 객체
     */
    @Bean(name = "cpfJdbcTemplate")
    public JdbcTemplate cpfJdbcTemplate(@Qualifier("cpfDataSource") DataSource cpfDataSource) {
        return new JdbcTemplate(cpfDataSource);
    }
}
