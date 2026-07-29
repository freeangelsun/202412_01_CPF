package com.cpf.member.config;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * CPF Platform 공통 DataSource 환경변수가 존재해도 Generated Domain은 자기 namespace만 사용합니다.
 */
class MemberDataSourceIsolationTest {

    @Test
    void resolvesOnlyDomainSpecificDataSourceProperties() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.db.vendor", "mariadb")
                .withProperty("cpf.datasource.mode", "url")
                .withProperty("cpf.datasource.url", "jdbc:mariadb://127.0.0.1:3306/cpfDB")
                .withProperty("cpf.datasource.username", "cpf_core_app")
                .withProperty("cpf.datasource.password", "core-secret")
                .withProperty("cpf.member.datasource.mode", "url")
                .withProperty("cpf.member.datasource.url", "jdbc:mariadb://127.0.0.1:3306/mbrDB")
                .withProperty("cpf.member.datasource.username", "cpf_member_app")
                .withProperty("cpf.member.datasource.password", "domain-secret");

        try (HikariDataSource dataSource = (HikariDataSource)
                new MemberDataSourceConfig().memberDataSource(environment)) {
            assertThat(dataSource.getJdbcUrl())
                    .isEqualTo("jdbc:mariadb://127.0.0.1:3306/mbrDB");
            assertThat(dataSource.getUsername()).isEqualTo("cpf_member_app");
        }
    }
}