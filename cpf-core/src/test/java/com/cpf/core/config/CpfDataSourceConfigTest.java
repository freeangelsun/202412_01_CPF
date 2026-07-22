package com.cpf.core.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jndi.JndiTemplate;

import javax.sql.DataSource;

/** CPF 메타 접근 객체가 동일한 CPF DataSource를 공유하는지 검증합니다. */
class CpfDataSourceConfigTest {

    @Test
    void createsUrlDataSourceFromCanonicalCpfProperties() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.cpf.mode", "url")
                .withProperty("spring.datasource.cpf.url", "jdbc:mariadb://localhost:3306/cpfDB")
                .withProperty("spring.datasource.cpf.username", "cpf_app")
                .withProperty("spring.datasource.cpf.password", "test-password")
                .withProperty("spring.datasource.cpf.driver-class-name", "org.mariadb.jdbc.Driver");

        DataSource dataSource = new CpfDataSourceConfig().cpfDataSource(environment, mock(JndiTemplate.class));

        assertThat(dataSource).isInstanceOf(HikariDataSource.class);
        HikariDataSource hikari = (HikariDataSource) dataSource;
        assertThat(hikari.getJdbcUrl()).isEqualTo("jdbc:mariadb://localhost:3306/cpfDB");
        assertThat(hikari.getUsername()).isEqualTo("cpf_app");
        hikari.close();
    }

    @Test
    void resolvesJndiDataSourceWhenExternalWasModeIsSelected() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.cpf.mode", "jndi")
                .withProperty("spring.datasource.cpf.jndi-name", "java:comp/env/jdbc/cpfCoreDataSource");
        DataSource expected = mock(DataSource.class);
        JndiTemplate jndiTemplate = mock(JndiTemplate.class);
        when(jndiTemplate.lookup("java:comp/env/jdbc/cpfCoreDataSource", DataSource.class)).thenReturn(expected);

        DataSource actual = new CpfDataSourceConfig().cpfDataSource(environment, jndiTemplate);

        assertThat(actual).isSameAs(expected);
    }

    @Test
    void rejectsUnknownConnectionMode() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("spring.datasource.cpf.mode", "direct");

        assertThatThrownBy(() -> new CpfDataSourceConfig()
                .cpfDataSource(environment, mock(JndiTemplate.class)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("url 또는 jndi");
    }

    @Test
    void createsCpfMetadataAccessObjects() {
        DataSource dataSource = mock(DataSource.class);
        CpfDataSourceConfig config = new CpfDataSourceConfig();

        JdbcTemplate jdbcTemplate = config.cpfJdbcTemplate(dataSource);
        DataSourceTransactionManager transactionManager =
                (DataSourceTransactionManager) config.cpfTransactionManager(dataSource);

        assertThat(jdbcTemplate.getDataSource()).isSameAs(dataSource);
        assertThat(transactionManager.getDataSource()).isSameAs(dataSource);
    }
}
