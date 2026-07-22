package cpf.pfw.config;

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
 * PFW 프레임워크 메타 DB 연결을 구성합니다.
 *
 * <p>거래 로그, 거래 메타, 로그 정책, 캐시 이벤트, 배치 운영 메타처럼 프레임워크가
 * 직접 소유하는 데이터는 pfwDB에 저장합니다.</p>
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

    /**
     * 모든 실행 모듈이 PFW 소유 메타를 같은 이름으로 접근하도록 공통 JDBC 객체를 제공합니다.
     *
     * <p>표준 실행 카탈로그, 거래 메타, 배치 관제, 서비스 레지스트리처럼 PFW가 소유하는
     * 저장소는 업무 모듈이 별도 객체를 중복 선언하지 않고 이 bean을 사용합니다.</p>
     *
     * @param pfwDataSource PFW 메타 DB 연결
     * @return PFW 메타 전용 JDBC 접근 객체
     */
    @Bean(name = "pfwJdbcTemplate")
    public JdbcTemplate pfwJdbcTemplate(@Qualifier("pfwDataSource") DataSource pfwDataSource) {
        return new JdbcTemplate(pfwDataSource);
    }
}
