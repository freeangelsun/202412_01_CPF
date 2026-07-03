package cpf.exs.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

/**
 * EXS 전용 DB 연결 설정입니다.
 *
 * <p>외부 연계 토큰, 통제 정책, 재처리 요청, 송수신 추적 로그를 exsDB에 저장합니다.
 * EXS는 송수신 원장을 남기는 것이 기본 동작이므로 local/runtime smoke에서는 datasource를 기본 활성화합니다.
 * DB 없이 EXS 샘플만 띄워야 하는 특수 상황에서는 {@code CPF_EXS_DB_ENABLED=false}로 명시 비활성화합니다.</p>
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(prefix = "cpf.exs.datasource", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ExsDataSourceConfig {

    /**
     * EXS DB 접속 속성을 바인딩합니다.
     */
    @Bean
    @ConfigurationProperties("cpf.exs.datasource")
    public DataSourceProperties exsDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * EXS 외부 연계 DB datasource입니다.
     */
    @Bean(name = "exsDataSource")
    public DataSource exsDataSource(DataSourceProperties exsDataSourceProperties) {
        return DataSourceBuilder.create()
                .driverClassName(exsDataSourceProperties.getDriverClassName())
                .url(exsDataSourceProperties.getUrl())
                .username(exsDataSourceProperties.getUsername())
                .password(exsDataSourceProperties.getPassword())
                .build();
    }

    /**
     * EXS SQL 실행용 named parameter JDBC template입니다.
     */
    @Bean(name = "exsJdbcTemplate")
    public NamedParameterJdbcTemplate exsJdbcTemplate(@Qualifier("exsDataSource") DataSource exsDataSource) {
        return new NamedParameterJdbcTemplate(exsDataSource);
    }

    /**
     * EXS DB transaction manager입니다.
     */
    @Bean(name = "exsTransactionManager")
    public PlatformTransactionManager exsTransactionManager(@Qualifier("exsDataSource") DataSource exsDataSource) {
        return new DataSourceTransactionManager(exsDataSource);
    }
}
