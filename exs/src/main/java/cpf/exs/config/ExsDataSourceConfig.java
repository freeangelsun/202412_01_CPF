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
 * <p>대외 token, 통제 정책, 재처리, 송수신 로그는 DB 영속화를 표준으로 삼습니다. 로컬 교육 기동과
 * 실제 DB 연동을 분리하기 위해 명시적으로 활성화한 경우에만 datasource를 생성합니다.</p>
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(prefix = "cpf.exs.datasource", name = "enabled", havingValue = "true")
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
     * EXS 대외 연계 DB datasource입니다.
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
