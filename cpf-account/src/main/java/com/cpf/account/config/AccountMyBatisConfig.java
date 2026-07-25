package com.cpf.account.config;

import com.cpf.core.api.database.CpfSqlResources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

/** ACC mapper를 ACC 소유 DataSource와 명시적으로 연결합니다. */
@Configuration(proxyBeanMethods = false)
public class AccountMyBatisConfig {

    @Bean(name = "accSqlSessionFactory")
    public SqlSessionFactory accSqlSessionFactory(
            @Qualifier("accDataSource") DataSource dataSource,
            Environment environment) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(CpfSqlResources.mapperResources(environment, "acc"));
        return factoryBean.getObject();
    }

    @Bean(name = "accSqlSessionTemplate")
    public SqlSessionTemplate accSqlSessionTemplate(
            @Qualifier("accSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
