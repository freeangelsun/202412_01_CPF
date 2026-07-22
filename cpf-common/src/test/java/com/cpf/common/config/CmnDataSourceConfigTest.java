package com.cpf.common.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;

/** CMN 기술 DB와 선택형 업무 공통 DB가 서로 다른 설정 경계를 사용하는지 검증합니다. */
class CmnDataSourceConfigTest {

    @Test
    void createsCoreMetadataDataSourceFromCmnPrefix() throws Exception {
        MockEnvironment environment = urlEnvironment(
                "spring.datasource.cmn",
                "jdbc:mariadb://localhost:3306/cpfDB",
                "cpf_app");

        DataSource result = new CmnDataSourceConfig().cmnDataSource(environment);

        assertThat(result).isInstanceOf(HikariDataSource.class);
        HikariDataSource hikari = (HikariDataSource) result;
        assertThat(hikari.getJdbcUrl()).endsWith("/cpfDB");
        assertThat(hikari.getUsername()).isEqualTo("cpf_app");
        hikari.close();
    }

    @Test
    void createsOptionalBusinessDataSourceFromDedicatedPrefix() throws Exception {
        MockEnvironment environment = urlEnvironment(
                "spring.datasource.cmn-business",
                "jdbc:mariadb://localhost:3306/cmnDB",
                "cpf_cmn_app");

        DataSource result = new CmnBusinessDataSourceConfig().cmnBusinessDataSource(environment);

        assertThat(result).isInstanceOf(HikariDataSource.class);
        HikariDataSource hikari = (HikariDataSource) result;
        assertThat(hikari.getJdbcUrl()).endsWith("/cmnDB");
        assertThat(hikari.getUsername()).isEqualTo("cpf_cmn_app");
        hikari.close();
    }

    private MockEnvironment urlEnvironment(String prefix, String url, String username) {
        return new MockEnvironment()
                .withProperty(prefix + ".mode", "url")
                .withProperty(prefix + ".url", url)
                .withProperty(prefix + ".username", username)
                .withProperty(prefix + ".password", "test-password");
    }
}
