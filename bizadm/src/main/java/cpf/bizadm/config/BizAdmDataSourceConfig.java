package cpf.bizadm.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * BIZADM 전용 DB 연결 설정입니다.
 *
 * <p>BIZADM은 업무 관리자 기본 구현체이므로 DB 영속화를 표준으로 삼습니다. 다만 교육/문서 확인을 위해
 * 로컬에서 DB 없이도 애플리케이션을 기동할 수 있도록 명시적으로 활성화한 경우에만 datasource를 생성합니다.</p>
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(prefix = "cpf.bizadm.datasource", name = "enabled", havingValue = "true")
public class BizAdmDataSourceConfig {

    /**
     * BIZADM DB 접속 속성을 바인딩합니다.
     */
    @Bean
    @ConfigurationProperties("cpf.bizadm.datasource")
    public DataSourceProperties bizAdmDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * BIZADM 업무 관리자 DB datasource입니다.
     */
    @Bean(name = "bizAdmDataSource")
    public DataSource bizAdmDataSource(DataSourceProperties bizAdmDataSourceProperties) {
        return DataSourceBuilder.create()
                .driverClassName(bizAdmDataSourceProperties.getDriverClassName())
                .url(bizAdmDataSourceProperties.getUrl())
                .username(bizAdmDataSourceProperties.getUsername())
                .password(bizAdmDataSourceProperties.getPassword())
                .build();
    }

    /**
     * BIZADM SQL 실행용 named parameter JDBC template입니다.
     */
    @Bean(name = "bizAdmJdbcTemplate")
    public NamedParameterJdbcTemplate bizAdmJdbcTemplate(@Qualifier("bizAdmDataSource") DataSource bizAdmDataSource) {
        return new NamedParameterJdbcTemplate(bizAdmDataSource);
    }

    /**
     * BIZADM DB transaction manager입니다.
     */
    @Bean(name = "bizAdmTransactionManager")
    public PlatformTransactionManager bizAdmTransactionManager(@Qualifier("bizAdmDataSource") DataSource bizAdmDataSource) {
        return new DataSourceTransactionManager(bizAdmDataSource);
    }
}
