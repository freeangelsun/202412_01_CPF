package cpf.xyz.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * XYZ 업무 DB 연결을 구성합니다.
 *
 * <p>XYZ는 교육 모듈이지만 신규 업무 모듈이 자기 업무 DB를 붙이는 기준을 보여줘야 하므로
 * CMN/PFW datasource와 별도로 xyzDB datasource를 둡니다.</p>
 */
@Configuration
public class XyzDataSourceConfig {
    @Value("${spring.datasource.xyz.url}")
    private String url;

    @Value("${spring.datasource.xyz.username}")
    private String username;

    @Value("${spring.datasource.xyz.password}")
    private String password;

    @Value("${spring.datasource.xyz.driver-class-name}")
    private String driverClassName;

    @Bean(name = "xyzDataSource")
    public DataSource xyzDataSource() {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    @Bean(name = "xyzJdbcTemplate")
    public JdbcTemplate xyzJdbcTemplate(@Qualifier("xyzDataSource") DataSource xyzDataSource) {
        return new JdbcTemplate(xyzDataSource);
    }

    @Bean(name = "xyzTransactionManager")
    public PlatformTransactionManager xyzTransactionManager(@Qualifier("xyzDataSource") DataSource xyzDataSource) {
        return new DataSourceTransactionManager(xyzDataSource);
    }
}
