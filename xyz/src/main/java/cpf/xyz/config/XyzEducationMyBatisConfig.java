package cpf.xyz.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

/**
 * XYZ EDU 조회 샘플에서 사용하는 MyBatis 설정입니다.
 *
 * <p>운영 코드가 아니라 교육 샘플이므로 별도 DB를 만들지 않고, 프로젝트 공통 영역인 cmnDB의 교육용 테이블을
 * 조회합니다. 실제 업무 모듈에서는 같은 패턴으로 자기 업무 DB datasource와 Mapper 위치를 분리하면 됩니다.</p>
 */
@Configuration
@MapperScan(basePackages = "cpf.xyz.edu.query.adapter", sqlSessionFactoryRef = "xyzEduSqlSessionFactory")
public class XyzEducationMyBatisConfig {
    private final DataSource cmnDataSource;

    public XyzEducationMyBatisConfig(@Qualifier("cmnDataSource") DataSource cmnDataSource) {
        this.cmnDataSource = cmnDataSource;
    }

    @Bean(name = "xyzEduSqlSessionFactory")
    public SqlSessionFactory xyzEduSqlSessionFactory() throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(cmnDataSource);
        factoryBean.setConfigLocation(new ClassPathResource("mybatis/config/cmn-mybatis-config.xml"));
        factoryBean.setMapperLocations(
                new PathMatchingResourcePatternResolver()
                        .getResources("classpath:mybatis/mapper/xyz/edu/**/*.xml")
        );
        return factoryBean.getObject();
    }
}
