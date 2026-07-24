package com.cpf.common.config;

import com.cpf.core.common.database.CpfSqlResourceResolver;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.annotations.Mapper;
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
 * CMN 모듈 MyBatis 설정입니다.
 * 공통 코드, 메시지, 캐시 이벤트, 업무 공통 기능 Mapper를 같은 SqlSessionFactory로 연결합니다.
 */
@Configuration
@MapperScan(
        basePackages = "com.cpf.common",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "cmnSqlSessionFactory")
public class CmnMyBatisConfig {

    private final DataSource cmnDataSource;
    private final Environment environment;

    /**
     * CMN 기준 datasource를 주입합니다.
     *
     * @param cmnDataSource CMN 공통 datasource
     */
    public CmnMyBatisConfig(
            @Qualifier("cmnDataSource") DataSource cmnDataSource,
            Environment environment) {
        this.cmnDataSource = cmnDataSource;
        this.environment = environment;
    }

    @Bean(name = "cmnSqlSessionFactory")
    public SqlSessionFactory cmnSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(cmnDataSource);
        sqlSessionFactoryBean.setConfigLocation(
                new ClassPathResource("mybatis/config/cmn-mybatis-config.xml")
        );
        sqlSessionFactoryBean.setMapperLocations(CpfSqlResourceResolver.mapperResources(environment, "cmn"));
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "cmnSqlSessionTemplate")
    public SqlSessionTemplate cmnSqlSessionTemplate(
            @Qualifier("cmnSqlSessionFactory") SqlSessionFactory cmnSqlSessionFactory) {
        return new SqlSessionTemplate(cmnSqlSessionFactory);
    }
}
