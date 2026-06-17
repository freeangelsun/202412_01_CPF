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
 * ADM에서 사용하는 DB 연결을 구성합니다.
 *
 * <p>ADM 운영 메타는 admDB, 프레임워크 로그/설정 조회는 pfwDB, 회원 운영 화면은
 * mbrDB를 사용합니다. 운영 환경에서는 각 datasource의 계정과 비밀번호를 환경변수로
 * 주입해야 합니다.</p>
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

    @Value("${spring.datasource.mbr.url:jdbc:mariadb://localhost:3306/mbrDB}")
    private String mbrUrl;

    @Value("${spring.datasource.mbr.username:cpf_mbr_app}")
    private String mbrUsername;

    @Value("${spring.datasource.mbr.password:cpf_local_pw}")
    private String mbrPassword;

    @Value("${spring.datasource.mbr.driver-class-name:org.mariadb.jdbc.Driver}")
    private String mbrDriverClassName;

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

    @Bean(name = "pfwJdbcTemplate")
    public JdbcTemplate pfwJdbcTemplate(@Qualifier("pfwDataSource") DataSource pfwDataSource) {
        return new JdbcTemplate(pfwDataSource);
    }

    @Bean(name = "mbrAdmDataSource")
    public DataSource mbrAdmDataSource() {
        return DataSourceBuilder.create()
                .url(mbrUrl)
                .username(mbrUsername)
                .password(mbrPassword)
                .driverClassName(mbrDriverClassName)
                .build();
    }

    @Bean(name = "mbrAdmTransactionManager")
    public PlatformTransactionManager mbrAdmTransactionManager(@Qualifier("mbrAdmDataSource") DataSource mbrAdmDataSource) {
        return new DataSourceTransactionManager(mbrAdmDataSource);
    }

    @Bean(name = "mbrJdbcTemplate")
    public JdbcTemplate mbrJdbcTemplate(@Qualifier("mbrAdmDataSource") DataSource mbrAdmDataSource) {
        return new JdbcTemplate(mbrAdmDataSource);
    }
}
