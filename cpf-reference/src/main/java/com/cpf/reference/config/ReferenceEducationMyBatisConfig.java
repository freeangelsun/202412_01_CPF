package com.cpf.reference.config;

import com.cpf.core.common.database.CpfSqlResourceResolver;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

/**
 * REF 기준 업무 조회 샘플에서 사용하는 MyBatis 설정입니다.
 *
 * <p>REF가 자기 업무 DB와 Mapper를 소유하는 기준을 보여주며 CMN/CPF DB를 직접 사용하지 않습니다.</p>
 */
@Configuration
@MapperScan(basePackages = "com.cpf.reference.query.adapter", sqlSessionFactoryRef = "refEduSqlSessionFactory")
public class ReferenceEducationMyBatisConfig {
    private final DataSource refDataSource;
    private final Environment environment;

    public ReferenceEducationMyBatisConfig(
            @Qualifier("refDataSource") DataSource refDataSource,
            Environment environment) {
        this.refDataSource = refDataSource;
        this.environment = environment;
    }

    @Bean(name = "refEduSqlSessionFactory")
    public SqlSessionFactory refEduSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(refDataSource);
        factoryBean.setConfigLocation(new ClassPathResource("mybatis/config/ref-mybatis-config.xml"));
        factoryBean.setMapperLocations(CpfSqlResourceResolver.mapperResources(environment, "ref"));
        return factoryBean.getObject();
    }
}
