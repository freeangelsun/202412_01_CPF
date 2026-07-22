package cpf.mbr.config;

import cpf.mbr.bse.mapper.MemberMapper;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
public class MbrMyBatisConfig {

    private final DataSource dataSource;

    public MbrMyBatisConfig(@Qualifier("mbrDataSource") DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Bean(name = "mbrSqlSessionFactory")
    public SqlSessionFactory mbrSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        sqlSessionFactoryBean.setConfigLocation(
                new ClassPathResource("mybatis/config/mbr-mybatis-config.xml")
        );
        sqlSessionFactoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mybatis/mapper/mbr/**/*.xml")
        );
        sqlSessionFactoryBean.setTypeAliasesPackage("cpf.mbr.bse.entity");

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

