package cpf.bza.config;

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
 * BZA 전용 DB 연결 설정입니다.
 *
 * <p>BZA은 업무 관리자 기본 구현체이므로 DB 영속화를 표준으로 삼습니다. 다만 교육/문서 확인을 위해
 * 로컬에서 DB 없이도 애플리케이션을 기동할 수 있도록 명시적으로 활성화한 경우에만 datasource를 생성합니다.</p>
 */
@Configuration
@EnableConfigurationProperties
@ConditionalOnProperty(prefix = "cpf.bza.datasource", name = "enabled", havingValue = "true")
public class BzaDataSourceConfig {

    /**
     * BZA DB 접속 속성을 바인딩합니다.
     */
    @Bean
    @ConfigurationProperties("cpf.bza.datasource")
    public DataSourceProperties bzaDataSourceProperties() {
        return new DataSourceProperties();
    }

    /**
     * BZA 업무 관리자 DB datasource입니다.
     */
    @Bean(name = "bzaDataSource")
    public DataSource bzaDataSource(DataSourceProperties bzaDataSourceProperties) {
        return DataSourceBuilder.create()
                .driverClassName(bzaDataSourceProperties.getDriverClassName())
                .url(bzaDataSourceProperties.getUrl())
                .username(bzaDataSourceProperties.getUsername())
                .password(bzaDataSourceProperties.getPassword())
                .build();
    }

    /**
     * BZA SQL 실행용 named parameter JDBC template입니다.
     */
    @Bean(name = "bzaJdbcTemplate")
    public NamedParameterJdbcTemplate bzaJdbcTemplate(@Qualifier("bzaDataSource") DataSource bzaDataSource) {
        return new NamedParameterJdbcTemplate(bzaDataSource);
    }

    /**
     * BZA DB transaction manager입니다.
     */
    @Bean(name = "bzaTransactionManager")
    public PlatformTransactionManager bzaTransactionManager(@Qualifier("bzaDataSource") DataSource bzaDataSource) {
        return new DataSourceTransactionManager(bzaDataSource);
    }
}
