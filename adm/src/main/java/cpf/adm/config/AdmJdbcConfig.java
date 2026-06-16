package cpf.adm.config;

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
 * ADM?먯꽌 ?ㅻⅨ ?꾨젅?꾩썙???ㅽ궎留덈? 議고쉶?섍린 ?꾪븳 JdbcTemplate ?ㅼ젙?낅땲??
 */
@Configuration
public class AdmJdbcConfig {
    @Value("${spring.datasource.adm.url:jdbc:mariadb://localhost:3306/admDB}")
    private String admUrl;

    @Value("${spring.datasource.adm.username:root}")
    private String admUsername;

    @Value("${spring.datasource.adm.password:}")
    private String admPassword;

    @Value("${spring.datasource.adm.driver-class-name:org.mariadb.jdbc.Driver}")
    private String admDriverClassName;

    @Bean(name = "admDataSource")
    public DataSource admDataSource() {
        return DataSourceBuilder.create()
                .url(admUrl)
                .username(admUsername)
                .password(admPassword)
                .driverClassName(admDriverClassName)
                .build();
    }

    @Bean(name = "admTransactionManager")
    public PlatformTransactionManager admTransactionManager(@Qualifier("admDataSource") DataSource admDataSource) {
        return new DataSourceTransactionManager(admDataSource);
    }

    @Bean(name = "admJdbcTemplate")
    public JdbcTemplate admJdbcTemplate(@Qualifier("admDataSource") DataSource admDataSource) {
        return new JdbcTemplate(admDataSource);
    }

    /**
     * PFW 濡쒓렇 ?뚯씠釉?議고쉶 ?꾩슜 JdbcTemplate?낅땲??
     *
     * @param pfwDataSource PFW ?꾨젅?꾩썙??DataSource
     * @return PFW JdbcTemplate
     */
    @Bean(name = "pfwJdbcTemplate")
    public JdbcTemplate pfwJdbcTemplate(@Qualifier("pfwDataSource") DataSource pfwDataSource) {
        return new JdbcTemplate(pfwDataSource);
    }
}

