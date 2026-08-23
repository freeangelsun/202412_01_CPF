package com.cpf.common.runtime;

import com.cpf.common.spi.CpfCommonPersistenceNames;
import com.cpf.data.persistence.api.CpfDataSourceRegistry;
import com.cpf.data.persistence.api.CpfDatabaseRole;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CpfCommonJdbcAutoConfigurationTest {

    @Test
    void exposesOnePlatformDatabaseObjectUnderStableCommonAndPlatformAliases() throws Exception {
        assertBeanNames("cpfCommonDataSource",
                CpfCommonPersistenceNames.DATA_SOURCE_BEAN,
                CpfCommonPersistenceNames.PLATFORM_DATA_SOURCE_BEAN);
        assertBeanNames("cpfCommonJdbcTemplate",
                CpfCommonPersistenceNames.JDBC_TEMPLATE_BEAN,
                CpfCommonPersistenceNames.PLATFORM_JDBC_TEMPLATE_BEAN);
        assertBeanNames("cpfCommonTransactionManager",
                CpfCommonPersistenceNames.TX_MANAGER_BEAN,
                CpfCommonPersistenceNames.PLATFORM_TX_MANAGER_BEAN);

        CpfDataSourceRegistry registry = mock(CpfDataSourceRegistry.class);
        DataSource platform = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(registry.require(CpfDatabaseRole.CPF_PLATFORM_DB)).thenReturn(platform);
        when(platform.getConnection()).thenReturn(connection);
        when(connection.isValid(3)).thenReturn(true);

        DataSource resolved = new CpfCommonJdbcAutoConfiguration()
                .cpfCommonDataSource(registry, new MockEnvironment());

        assertThat(resolved).isSameAs(platform);
        verify(registry).require(CpfDatabaseRole.CPF_PLATFORM_DB);
    }

    private static void assertBeanNames(String methodName, String... expected) throws Exception {
        Method method = List.of(CpfCommonJdbcAutoConfiguration.class.getDeclaredMethods()).stream()
                .filter(candidate -> candidate.getName().equals(methodName))
                .findFirst()
                .orElseThrow();
        assertThat(method.getAnnotation(Bean.class).name()).containsExactly(expected);
    }
}
