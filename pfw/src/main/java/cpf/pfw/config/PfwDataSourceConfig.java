package cpf.pfw.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * CPF 기능 설명입니다.
 */
@Configuration
public class PfwDataSourceConfig {

    @Value("${spring.datasource.pfw.url}")
    private String url;

    @Value("${spring.datasource.pfw.username}")
    private String username;

    @Value("${spring.datasource.pfw.password}")
    private String password;

    @Value("${spring.datasource.pfw.driver-class-name}")
    private String driverClassName;

    @Bean(name = "pfwDataSource")
    public DataSource pfwDataSource() {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    @Bean(name = "pfwTransactionManager")
    public PlatformTransactionManager pfwTransactionManager(@Qualifier("pfwDataSource") DataSource pfwDataSource) {
        return new DataSourceTransactionManager(pfwDataSource);
    }
}

