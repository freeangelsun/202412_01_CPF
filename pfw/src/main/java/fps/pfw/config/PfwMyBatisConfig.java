package fps.pfw.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * PFW 프레임워크 필수 테이블용 MyBatis 설정입니다.
 */
@Configuration
@MapperScan(basePackages = "fps.pfw.mapper", sqlSessionFactoryRef = "pfwSqlSessionFactory")
public class PfwMyBatisConfig {

    private final DataSource pfwDataSource;

    public PfwMyBatisConfig(@Qualifier("pfwDataSource") DataSource pfwDataSource) {
        this.pfwDataSource = pfwDataSource;
    }

    @Bean(name = "pfwSqlSessionFactory")
    public SqlSessionFactory pfwSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(pfwDataSource);
        sqlSessionFactoryBean.setConfigLocation(new ClassPathResource("mybatis/config/pfw-mybatis-config.xml"));
        sqlSessionFactoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mybatis/mapper/pfw/**/*.xml")
        );
        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "pfwSqlSessionTemplate")
    public SqlSessionTemplate pfwSqlSessionTemplate(
            @Qualifier("pfwSqlSessionFactory") SqlSessionFactory pfwSqlSessionFactory) {
        return new SqlSessionTemplate(pfwSqlSessionFactory);
    }
}
