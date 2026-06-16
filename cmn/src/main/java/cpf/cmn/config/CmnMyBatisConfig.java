package cpf.cmn.config;

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

@Configuration
@MapperScan(basePackages = "cpf.cmn", sqlSessionFactoryRef = "cmnSqlSessionFactory") // CMN ?섏쐞 ?꾨찓?몄쓽 留ㅽ띁 ?명꽣?섏씠?ㅻ? ?ㅼ틪?⑸땲??
public class CmnMyBatisConfig {

    private final DataSource cmnDataSource;

    // DataSource 紐낇솗?섍쾶 吏??
    public CmnMyBatisConfig(@Qualifier("cmnDataSource") DataSource cmnDataSource) {
        this.cmnDataSource = cmnDataSource;
    }

    @Bean(name = "cmnSqlSessionFactory")
    public SqlSessionFactory cmnSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(cmnDataSource);

        // MyBatis ?ㅼ젙 ?뚯씪 吏??
        sqlSessionFactoryBean.setConfigLocation(
                new ClassPathResource("mybatis/config/cmn-mybatis-config.xml")
        );

        // 留ㅽ띁 XML ?뚯씪 寃쎈줈 吏??
        sqlSessionFactoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mybatis/mapper/cmn/**/*.xml")
        );

        return sqlSessionFactoryBean.getObject();
    }

    @Bean(name = "cmnSqlSessionTemplate")
    public SqlSessionTemplate cmnSqlSessionTemplate(
            @Qualifier("cmnSqlSessionFactory") SqlSessionFactory cmnSqlSessionFactory) {
        return new SqlSessionTemplate(cmnSqlSessionFactory);
    }
}

