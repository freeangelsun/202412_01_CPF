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
 * ADM 운영 화면에서 사용하는 DB 연결을 구성합니다.
 *
 * <p>ADM은 운영 메타 admDB, 프레임워크 로그/설정 pfwDB, 회원 운영 mbrDB,
 * EDU 업무 샘플 xyzDB, 외부연계 송수신 원장 exsDB를 조회합니다. 운영 환경에서는
 * 각 datasource 계정과 비밀번호를 환경변수 또는 Vault/KMS 연동 값으로 주입해야 합니다.</p>
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

    @Value("${spring.datasource.xyz.url:jdbc:mariadb://localhost:3306/xyzDB}")
    private String xyzUrl;

    @Value("${spring.datasource.xyz.username:cpf_xyz_app}")
    private String xyzUsername;

    @Value("${spring.datasource.xyz.password:cpf_local_pw}")
    private String xyzPassword;

    @Value("${spring.datasource.xyz.driver-class-name:org.mariadb.jdbc.Driver}")
    private String xyzDriverClassName;

    @Value("${spring.datasource.exs.url:jdbc:mariadb://localhost:3306/exsDB}")
    private String exsUrl;

    @Value("${spring.datasource.exs.username:cpf_exs_app}")
    private String exsUsername;

    @Value("${spring.datasource.exs.password:cpf_local_pw}")
    private String exsPassword;

    @Value("${spring.datasource.exs.driver-class-name:org.mariadb.jdbc.Driver}")
    private String exsDriverClassName;

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

    @Bean(name = "xyzAdmDataSource")
    public DataSource xyzAdmDataSource() {
        return DataSourceBuilder.create()
                .url(xyzUrl)
                .username(xyzUsername)
                .password(xyzPassword)
                .driverClassName(xyzDriverClassName)
                .build();
    }

    @Bean(name = "xyzJdbcTemplate")
    public JdbcTemplate xyzJdbcTemplate(@Qualifier("xyzAdmDataSource") DataSource xyzAdmDataSource) {
        return new JdbcTemplate(xyzAdmDataSource);
    }

    @Bean(name = "exsAdmDataSource")
    public DataSource exsAdmDataSource() {
        return DataSourceBuilder.create()
                .url(exsUrl)
                .username(exsUsername)
                .password(exsPassword)
                .driverClassName(exsDriverClassName)
                .build();
    }

    @Bean(name = "exsJdbcTemplate")
    public JdbcTemplate exsJdbcTemplate(@Qualifier("exsAdmDataSource") DataSource exsAdmDataSource) {
        return new JdbcTemplate(exsAdmDataSource);
    }
}
