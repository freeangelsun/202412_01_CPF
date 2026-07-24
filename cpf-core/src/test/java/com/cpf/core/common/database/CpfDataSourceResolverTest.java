package com.cpf.core.common.database;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.cpf.core.api.database.CpfDatabaseVendor;
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
    void buildsVendorUrlAndDriverWhenExplicitUrlIsAbsent() throws Exception {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.db.vendor", "postgresql")
                .withProperty("cpf.db.host", "db.internal")
                .withProperty("cpf.db.port", "5544")
                .withProperty("test.datasource.database-name", "cpfDB")
                .withProperty("test.datasource.username", "cpf_app")
                .withProperty("test.datasource.password", "not-a-secret");

        DataSource result = CpfDataSourceResolver.resolve(environment, "test.datasource");

        assertThat(result).isInstanceOf(HikariDataSource.class);
        HikariDataSource hikari = (HikariDataSource) result;
        assertThat(hikari.getJdbcUrl()).isEqualTo("jdbc:postgresql://db.internal:5544/cpfDB");
        assertThat(hikari.getDriverClassName()).isEqualTo("org.postgresql.Driver");
        hikari.close();
    }

    @Test
    void rejectsUrlThatDoesNotMatchSelectedVendor() {
        MockEnvironment environment = new MockEnvironment()
                .withProperty("cpf.db.vendor", "oracle")
                .withProperty("test.datasource.url", "jdbc:mariadb://localhost:3306/cpfDB")
                .withProperty("test.datasource.username", "cpf_app")
                .withProperty("test.datasource.password", "not-a-secret");

        assertThatThrownBy(() -> CpfDataSourceResolver.resolve(environment, "test.datasource"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cpf.db.vendor=oracle");
    }

    @Test
    void exposesAllSupportedVendorResourceContracts() {
        assertThat(CpfDatabaseVendor.values())
                .extracting(CpfDatabaseVendor::id)
                .containsExactly("mariadb", "mysql", "postgresql", "oracle", "sqlserver");
        assertThat(CpfDatabaseVendor.MARIADB.jdbcUrl("localhost", null, "cpfDB"))
                .isEqualTo("jdbc:mariadb://localhost:3306/cpfDB");
        assertThat(CpfDatabaseVendor.MYSQL.jdbcUrl("localhost", null, "cpfDB"))
                .isEqualTo("jdbc:mysql://localhost:3306/cpfDB");
        assertThat(CpfDatabaseVendor.POSTGRESQL.jdbcUrl("localhost", null, "cpfDB"))
                .isEqualTo("jdbc:postgresql://localhost:5432/cpfDB");
        assertThat(CpfDatabaseVendor.ORACLE.jdbcUrl("localhost", null, "CPF"))
                .isEqualTo("jdbc:oracle:thin:@//localhost:1521/CPF");
        assertThat(CpfDatabaseVendor.SQLSERVER.jdbcUrl("localhost", null, "cpfDB"))
                .isEqualTo("jdbc:sqlserver://localhost:1433;databaseName=cpfDB");
        assertThat(CpfDatabaseVendor.MARIADB.springBatchDatabaseType()).isEqualTo("MARIADB");
        assertThat(CpfDatabaseVendor.MYSQL.springBatchDatabaseType()).isEqualTo("MYSQL");
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
