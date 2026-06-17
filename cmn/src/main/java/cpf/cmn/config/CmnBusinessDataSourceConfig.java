package cpf.cmn.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * CMN 업무 공통 DB(cmnDB)를 선택적으로 연결하는 설정입니다.
 *
 * <p>PFW 코드/메시지/응답코드 캐시는 기존 PFW datasource를 사용하고, 채번/알림/공통 업무 로그처럼
 * 여러 업무 모듈이 공유하는 비즈니스 공통 데이터는 별도 datasource를 사용합니다.</p>
 */
@Configuration
@ConditionalOnProperty(prefix = "cpf.cmn.business-db", name = "enabled", havingValue = "true")
public class CmnBusinessDataSourceConfig {

    @Value("${spring.datasource.cmn-business.url}")
    private String url;

    @Value("${spring.datasource.cmn-business.username}")
    private String username;

    @Value("${spring.datasource.cmn-business.password}")
    private String password;

    @Value("${spring.datasource.cmn-business.driver-class-name:org.mariadb.jdbc.Driver}")
    private String driverClassName;

    @Bean(name = "cmnBusinessDataSource")
    public DataSource cmnBusinessDataSource() {
        return DataSourceBuilder.create()
                .url(url)
                .username(username)
                .password(password)
                .driverClassName(driverClassName)
                .build();
    }

    @Bean(name = "cmnBusinessJdbcTemplate")
    public JdbcTemplate cmnBusinessJdbcTemplate(@Qualifier("cmnBusinessDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "cmnBusinessTransactionManager")
    public PlatformTransactionManager cmnBusinessTransactionManager(
            @Qualifier("cmnBusinessDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "cmnBusinessTransactionTemplate")
    public TransactionTemplate cmnBusinessTransactionTemplate(
            @Qualifier("cmnBusinessTransactionManager") PlatformTransactionManager transactionManager) {
        return new TransactionTemplate(transactionManager);
    }
}
