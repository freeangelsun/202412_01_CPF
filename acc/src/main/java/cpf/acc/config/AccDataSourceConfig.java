package cpf.acc.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class AccDataSourceConfig {

    @Value("${spring.datasource.acc.url}")
    private String url;

    @Value("${spring.datasource.acc.username}")
    private String username;

    @Value("${spring.datasource.acc.password}")
    private String password;

    @Value("${spring.datasource.acc.driver-class-name}")
    private String driverClassName;

    @Bean(name = "accDataSource")
    public DataSource accDataSource() {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

//    private final Environment env;
//
//    @Autowired
//    public AccDataSourceConfig(Environment env) {
//        this.env = env;
//    }
//
//    @Bean(name = "accDataSource")
//    @ConfigurationProperties(prefix = "spring.datasource.acc")
//    public DataSource accDataSource() {
//        return DataSourceBuilder.create().build();
//    }
//
//    /*
//    @Bean(name = "accDataSource")
//    public DataSource accDataSource() throws Exception {
//        String jndiName = env.getProperty("spring.datasource.acc.jndi-name");
//        return (DataSource) new JndiTemplate().lookup(jndiName);
//    }
//     */

}
