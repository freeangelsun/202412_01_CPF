package cpf.acc.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/** ACC mapper를 ACC 소유 DataSource와 명시적으로 연결합니다. */
@Configuration(proxyBeanMethods = false)
public class AccountMyBatisConfig {

    @Bean(name = "accSqlSessionFactory")
    public SqlSessionFactory accSqlSessionFactory(
            @Qualifier("accDataSource") DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mybatis/mapper/acc/**/*.xml"));
        return factoryBean.getObject();
    }

    @Bean(name = "accSqlSessionTemplate")
    public SqlSessionTemplate accSqlSessionTemplate(
            @Qualifier("accSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}
