package cpf.acc.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.annotations.Mapper;
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
 * CPF 기능 설명입니다.
 */
@Configuration
@MapperScan(
        basePackages = "cpf.acc",
        annotationClass = Mapper.class,
        sqlSessionFactoryRef = "accSqlSessionFactory"
)
public class AccMyBatisConfig {

    private final DataSource accDataSource;

    public AccMyBatisConfig(@Qualifier("accDataSource") DataSource accDataSource) {
        this.accDataSource = accDataSource;
    }

    /**
     * CPF 기능 설명입니다.
     */
    @Bean(name = "accSqlSessionFactory")
    public SqlSessionFactory accSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(accDataSource);

        // CPF 기능 설명입니다.
        sqlSessionFactoryBean.setConfigLocation(
                new ClassPathResource("mybatis/config/acc-mybatis-config.xml")
        );

        // CPF 기능 설명입니다.
        sqlSessionFactoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mybatis/mapper/acc/**/*.xml")
        );

        // CPF 기능 설명입니다.
        sqlSessionFactoryBean.setTypeAliasesPackage("cpf.acc.bse.entity");

        return sqlSessionFactoryBean.getObject();
    }

    /**
     * CPF 기능 설명입니다.
     */
    @Bean(name = "accSqlSessionTemplate")
    public SqlSessionTemplate accSqlSessionTemplate(
            @Qualifier("accSqlSessionFactory") SqlSessionFactory accSqlSessionFactory) {
        return new SqlSessionTemplate(accSqlSessionFactory);
    }
}
