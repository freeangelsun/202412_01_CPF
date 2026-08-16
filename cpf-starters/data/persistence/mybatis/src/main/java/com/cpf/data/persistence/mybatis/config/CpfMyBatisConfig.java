package com.cpf.data.persistence.mybatis.config;

import com.cpf.data.persistence.sql.CpfSqlResourceResolver;
import com.cpf.platform.operations.observability.spi.logging.CpfTransactionLogPersistencePort;
import com.cpf.platform.operations.observability.spi.logging.segment.CpfTransactionSegmentPersistencePort;
import com.cpf.platform.operations.observability.spi.logging.lineage.CpfTransactionLineageProjectionPort;
import com.cpf.data.persistence.mybatis.mapper.logging.TransactionLogMapper;
import com.cpf.data.persistence.mybatis.mapper.logging.TransactionSegmentMapper;
import com.cpf.data.persistence.mybatis.logging.MyBatisTransactionLogPersistenceAdapter;
import com.cpf.data.persistence.mybatis.logging.MyBatisTransactionSegmentPersistenceAdapter;
import com.cpf.data.persistence.mybatis.logging.JdbcTransactionLineageProjectionAdapter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

/**
 * CPF 코어 데이터소스 전용 MyBatis 세션과 매퍼 스캔 범위를 구성합니다.
 *
 * <p>업무 모듈의 세션과 분리된 이름을 사용해 다른 주제영역 DB를 직접 조회하는
 * 실수를 방지합니다.</p>
 */
@Configuration
@MapperScan(basePackages = "com.cpf.data.persistence.mybatis.mapper", sqlSessionFactoryRef = "cpfSqlSessionFactory")
public class CpfMyBatisConfig {

    private final DataSource cpfDataSource;
    private final Environment environment;

    /** CpfMyBatisConfig 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfMyBatisConfig(
            @Qualifier("cpfDataSource") DataSource cpfDataSource,
            Environment environment) {
        this.cpfDataSource = cpfDataSource;
        this.environment = environment;
    }

    @Bean(name = "cpfSqlSessionFactory")
    /** cpfSqlSessionFactory 작업을 CPF 표준 계약에 따라 수행한다. */
    public SqlSessionFactory cpfSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(cpfDataSource);
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("mybatis/config/cpf-mybatis-config.xml"));
        sqlSessionFactoryBean.setMapperLocations(CpfSqlResourceResolver.mapperResources(environment, "cpf"));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    /** cpfTransactionLogPersistencePort 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfTransactionLogPersistencePort cpfTransactionLogPersistencePort(TransactionLogMapper mapper) {
        return new MyBatisTransactionLogPersistenceAdapter(mapper);
    }

    @Bean
    public CpfTransactionSegmentPersistencePort cpfTransactionSegmentPersistencePort(TransactionSegmentMapper mapper) {
        return new MyBatisTransactionSegmentPersistenceAdapter(mapper);
    }

    @Bean
    /** cpfTransactionLineageProjectionPort 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfTransactionLineageProjectionPort cpfTransactionLineageProjectionPort() {
        return new JdbcTransactionLineageProjectionAdapter(new JdbcTemplate(cpfDataSource));
    }

    @Bean(name = "cpfSqlSessionTemplate")
    public SqlSessionTemplate cpfSqlSessionTemplate(
            @Qualifier("cpfSqlSessionFactory") SqlSessionFactory cpfSqlSessionFactory) {
        return new SqlSessionTemplate(cpfSqlSessionFactory);
    }
}
