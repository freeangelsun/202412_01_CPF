package cpf.bat.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * BAT에서 PFW 운영 메타를 조회할 때 사용하는 JDBC 보조 구성을 제공합니다.
 */
@Configuration
public class BatJdbcConfig {

    @Bean(name = "pfwJdbcTemplate")
    public JdbcTemplate pfwJdbcTemplate(@Qualifier("pfwDataSource") DataSource pfwDataSource) {
        return new JdbcTemplate(pfwDataSource);
    }
}
