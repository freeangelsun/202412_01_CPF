package com.cpf.education.common.config;
import com.cpf.data.persistence.mybatis.CpfSqlResources;
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
 * EDU 기준 업무 조회 샘플에서 사용하는 MyBatis 설정입니다.
 *
 * <p>EDU가 자기 업무 DB와 Mapper를 소유하는 기준을 보여주며 CMN/CPF DB를 직접 사용하지 않습니다.</p>
 */
@Configuration
@MapperScan(basePackages = "com.cpf.education.query.adapter", sqlSessionFactoryRef = "refEduSqlSessionFactory")
public class EducationEducationMyBatisConfig {
    private final DataSource educationReferenceFixtureDataSource;
    private final Environment environment;

    /** EducationEducationMyBatisConfig 작업을 CPF 표준 계약에 따라 수행한다. */
    public EducationEducationMyBatisConfig(
            @Qualifier("educationReferenceFixtureDataSource") DataSource educationReferenceFixtureDataSource,
            Environment environment) {
        this.educationReferenceFixtureDataSource = educationReferenceFixtureDataSource;
        this.environment = environment;
    }

    @Bean(name = "refEduSqlSessionFactory")
    /** refEduSqlSessionFactory 작업을 CPF 표준 계약에 따라 수행한다. */
    public SqlSessionFactory refEduSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(educationReferenceFixtureDataSource);
        factoryBean.setConfigLocation(new ClassPathResource("mybatis/config/ref-mybatis-config.xml"));
        factoryBean.setMapperLocations(CpfSqlResources.mapperResources(environment, "ref"));
        return factoryBean.getObject();
    }
}
