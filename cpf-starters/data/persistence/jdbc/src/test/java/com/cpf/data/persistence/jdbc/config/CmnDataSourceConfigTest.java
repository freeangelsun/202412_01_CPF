package com.cpf.data.persistence.jdbc.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import javax.sql.DataSource;
import static org.assertj.core.api.Assertions.assertThat;

/** cpfDB owner와 선택형 CMN sample DB 경계를 검증합니다. */
@SuppressWarnings("deprecation")
class CmnDataSourceConfigTest {
    @Test void legacyCommonDatasourceOwnerIsRetired() {
        assertThat(CmnDataSourceConfig.class.getDeclaredMethods()).isEmpty();
        assertThat(CmnDataSourceConfig.class.isAnnotationPresent(Deprecated.class)).isTrue();
    }

    @Test void createsOptionalSampleDataSourceFromDedicatedPrefix() throws Exception {
        MockEnvironment environment=urlEnvironment("spring.datasource.cmn-sample","jdbc:mariadb://localhost:3306/referenceFixture","cpf_cmn_app");
        DataSource result=new CmnSampleDataSourceConfig().cmnSampleDataSource(environment);
        assertThat(result).isInstanceOf(HikariDataSource.class);
        HikariDataSource hikari=(HikariDataSource)result;
        assertThat(hikari.getJdbcUrl()).endsWith("/referenceFixture");
        assertThat(hikari.getUsername()).isEqualTo("cpf_cmn_app");
        hikari.close();
    }

    private MockEnvironment urlEnvironment(String prefix,String url,String username){return new MockEnvironment().withProperty(prefix+".mode","url").withProperty(prefix+".url",url).withProperty(prefix+".username",username).withProperty(prefix+".password","test-password");}
}
