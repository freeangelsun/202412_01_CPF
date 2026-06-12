package fps.acc.config;

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
 * ACC 모듈의 MyBatis 설정 클래스
 */
@Configuration
@MapperScan(
        basePackages = "fps.acc",
        sqlSessionFactoryRef = "accSqlSessionFactory"
)
public class AccMyBatisConfig {

    private final DataSource accDataSource;

    public AccMyBatisConfig(@Qualifier("accDataSource") DataSource accDataSource) {
        this.accDataSource = accDataSource;
    }

    /**
     * ACC 전용 SqlSessionFactory 설정
     */
    @Bean(name = "accSqlSessionFactory")
    public SqlSessionFactory accSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(accDataSource);

        // MyBatis 전역 설정 파일
        sqlSessionFactoryBean.setConfigLocation(
                new ClassPathResource("mybatis/config/acc-mybatis-config.xml")
        );

        // 매퍼 XML 파일 경로
        sqlSessionFactoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mybatis/mapper/acc/**/*.xml")
        );

        // TypeAliases 패키지 설정 (옵션)
        sqlSessionFactoryBean.setTypeAliasesPackage("fps.acc.bse.entity");

        return sqlSessionFactoryBean.getObject();
    }

    /**
     * ACC 전용 SqlSessionTemplate 설정
     */
    @Bean(name = "accSqlSessionTemplate")
    public SqlSessionTemplate accSqlSessionTemplate(
            @Qualifier("accSqlSessionFactory") SqlSessionFactory accSqlSessionFactory) {
        return new SqlSessionTemplate(accSqlSessionFactory);
    }
}
