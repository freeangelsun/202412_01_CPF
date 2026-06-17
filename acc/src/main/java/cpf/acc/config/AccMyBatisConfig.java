package cpf.acc.config;

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
 * ACC ⑤뱢??MyBatis ?ㅼ젙 ?대옒??
 */
@Configuration
@MapperScan(
        basePackages = "cpf.acc",
        sqlSessionFactoryRef = "accSqlSessionFactory"
)
public class AccMyBatisConfig {

    private final DataSource accDataSource;

    public AccMyBatisConfig(@Qualifier("accDataSource") DataSource accDataSource) {
        this.accDataSource = accDataSource;
    }

    /**
     * ACC ?꾩슜 SqlSessionFactory ?ㅼ젙
     */
    @Bean(name = "accSqlSessionFactory")
    public SqlSessionFactory accSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(accDataSource);

        // MyBatis ?꾩뿭 ?ㅼ젙 ?뚯씪
        sqlSessionFactoryBean.setConfigLocation(
                new ClassPathResource("mybatis/config/acc-mybatis-config.xml")
        );

        // 留ㅽ띁 XML ?뚯씪 寃쎈줈
        sqlSessionFactoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mybatis/mapper/acc/**/*.xml")
        );

        // TypeAliases ?⑦궎吏 ?ㅼ젙 (?듭뀡)
        sqlSessionFactoryBean.setTypeAliasesPackage("cpf.acc.bse.entity");

        return sqlSessionFactoryBean.getObject();
    }

    /**
     * ACC ?꾩슜 SqlSessionTemplate ?ㅼ젙
     */
    @Bean(name = "accSqlSessionTemplate")
    public SqlSessionTemplate accSqlSessionTemplate(
            @Qualifier("accSqlSessionFactory") SqlSessionFactory accSqlSessionFactory) {
        return new SqlSessionTemplate(accSqlSessionFactory);
    }
}

