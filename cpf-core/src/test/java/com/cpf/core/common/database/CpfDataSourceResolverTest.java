package com.cpf.core.common.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.jndi.JndiTemplate;
import org.springframework.mock.env.MockEnvironment;

import javax.sql.DataSource;

class CpfDataSourceResolverTest {

    @Test
    void createsUrlDataSourceFromGivenPrefix() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("test.datasource.mode", "url")
                .withProperty("test.datasource.url", "jdbc:mariadb://localhost:3306/cpfDB")
                .withProperty("test.datasource.username", "cpf_app")
                .withProperty("test.datasource.password", "test-password");

        DataSource result = CpfDataSourceResolver.resolve(
                environment,
                "test.datasource",
                mock(JndiTemplate.class));

        assertThat(result).isInstanceOf(HikariDataSource.class);
        HikariDataSource hikari = (HikariDataSource) result;
        assertThat(hikari.getJdbcUrl()).isEqualTo("jdbc:mariadb://localhost:3306/cpfDB");
        assertThat(hikari.getUsername()).isEqualTo("cpf_app");
        hikari.close();
    }

    @Test
    void returnsJndiDataSourceWithoutReadingUrlSecrets() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("test.datasource.mode", "jndi")
                .withProperty("test.datasource.jndi-name", "java:comp/env/jdbc/cpfCoreDataSource");
        DataSource expected = mock(DataSource.class);
        JndiTemplate jndiTemplate = mock(JndiTemplate.class);
        when(jndiTemplate.lookup("java:comp/env/jdbc/cpfCoreDataSource", DataSource.class)).thenReturn(expected);

        DataSource result = CpfDataSourceResolver.resolve(environment, "test.datasource", jndiTemplate);

        assertThat(result).isSameAs(expected);
    }

    @Test
    void rejectsUnknownModeBeforeCreatingDataSource() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("test.datasource.mode", "automatic");

        assertThatThrownBy(() -> CpfDataSourceResolver.resolve(
                environment,
                "test.datasource",
                mock(JndiTemplate.class)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("url 또는 jndi");
    }
}
