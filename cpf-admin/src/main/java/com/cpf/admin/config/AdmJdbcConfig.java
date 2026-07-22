package com.cpf.admin.config;

import com.cpf.core.common.database.CpfDataSourceResolver;
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
 * ADM 운영 화면에서 사용하는 DB 연결을 구성합니다.
 *
 * <p>ADM은 운영 메타 admDB, 프레임워크 로그/설정 cpfDB, 회원 운영 mbrDB,
 * EDU 업무 샘플 refDB를 조회합니다. 운영 환경에서는
 * 각 datasource 계정과 비밀번호를 환경변수 또는 Vault/KMS 연동 값으로 주입해야 합니다.</p>
 */
@Configuration
public class AdmJdbcConfig {
    @Bean(name = "admDataSource")
    public DataSource admDataSource(Environment environment) throws NamingException {
        return CpfDataSourceResolver.resolve(environment, "spring.datasource.adm");
    }

    @Bean(name = "admTransactionManager")
    public PlatformTransactionManager admTransactionManager(@Qualifier("admDataSource") DataSource admDataSource) {
        return new DataSourceTransactionManager(admDataSource);
    }

    @Bean(name = "admJdbcTemplate")
    public JdbcTemplate admJdbcTemplate(@Qualifier("admDataSource") DataSource admDataSource) {
        return new JdbcTemplate(admDataSource);
    }

    @Bean(name = "mbrAdmDataSource")
    public DataSource mbrAdmDataSource(Environment environment) throws NamingException {
        return CpfDataSourceResolver.resolve(environment, "spring.datasource.mbr");
    }

    @Bean(name = "mbrAdmTransactionManager")
    public PlatformTransactionManager mbrAdmTransactionManager(@Qualifier("mbrAdmDataSource") DataSource mbrAdmDataSource) {
        return new DataSourceTransactionManager(mbrAdmDataSource);
    }

    @Bean(name = "mbrJdbcTemplate")
    public JdbcTemplate mbrJdbcTemplate(@Qualifier("mbrAdmDataSource") DataSource mbrAdmDataSource) {
        return new JdbcTemplate(mbrAdmDataSource);
    }

    @Bean(name = "refAdmDataSource")
    public DataSource refAdmDataSource(Environment environment) throws NamingException {
        return CpfDataSourceResolver.resolve(environment, "spring.datasource.ref");
    }

    @Bean(name = "refJdbcTemplate")
    public JdbcTemplate refJdbcTemplate(@Qualifier("refAdmDataSource") DataSource refAdmDataSource) {
        return new JdbcTemplate(refAdmDataSource);
    }

}
