package com.cpf.member.config;

import com.cpf.core.common.database.CpfSqlResourceResolver;
import com.cpf.member.bse.mapper.MemberMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;

import javax.sql.DataSource;

@Configuration
public class MbrMyBatisConfig {

    private final DataSource dataSource;
    private final Environment environment;

    public MbrMyBatisConfig(
            @Qualifier("mbrDataSource") DataSource dataSource,
            Environment environment) {
        this.dataSource = dataSource;
        this.environment = environment;
    }

    @Bean(name = "mbrSqlSessionFactory")
    public SqlSessionFactory mbrSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setConfigLocation(
                new ClassPathResource("mybatis/config/mbr-mybatis-config.xml")
        );
        sqlSessionFactoryBean.setMapperLocations(CpfSqlResourceResolver.mapperResources(environment, "mbr"));
        sqlSessionFactoryBean.setTypeAliasesPackage("com.cpf.member.bse.entity");

        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "mbrSqlSessionTemplate")
    public SqlSessionTemplate mbrSqlSessionTemplate(
            @Qualifier("mbrSqlSessionFactory") SqlSessionFactory mbrSqlSessionFactory) {
        return new SqlSessionTemplate(mbrSqlSessionFactory);
    }

    @Bean(name = "mbrMemberMapper")
    public MapperFactoryBean<MemberMapper> memberMapper(
            @Qualifier("mbrSqlSessionFactory") SqlSessionFactory mbrSqlSessionFactory) {
        MapperFactoryBean<MemberMapper> mapperFactoryBean = new MapperFactoryBean<>(MemberMapper.class);
        mapperFactoryBean.setSqlSessionFactory(mbrSqlSessionFactory);
        return mapperFactoryBean;
    }
}
