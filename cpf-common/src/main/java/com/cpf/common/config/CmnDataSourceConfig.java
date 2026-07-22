package com.cpf.common.config;

import com.cpf.core.common.database.CpfDataSourceResolver;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.naming.NamingException;
import javax.sql.DataSource;

/** CPF 기술 메타를 읽는 CMN 공통 데이터소스를 구성합니다. */
@Configuration
public class CmnDataSourceConfig {

    /** 배포 형태에 따라 URL 또는 JNDI 방식으로 cpfDB 연결을 생성합니다. */
    @Bean(name = "cmnDataSource")
    public DataSource cmnDataSource(Environment environment) throws NamingException {
        return CpfDataSourceResolver.resolve(environment, "spring.datasource.cmn");
    }

    /** 코드·메시지·설정 조회와 캐시 이벤트 기록에 사용하는 트랜잭션 관리자입니다. */
    @Bean(name = "cmnTransactionManager")
    public PlatformTransactionManager cmnTransactionManager(@Qualifier("cmnDataSource") DataSource cmnDataSource) {
        return new DataSourceTransactionManager(cmnDataSource);
    }
}

