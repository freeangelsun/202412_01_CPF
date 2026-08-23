package com.cpf.data.persistence.jdbc;

import com.cpf.data.persistence.api.CpfDatabaseRole;
import javax.sql.DataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.mock.env.MockEnvironment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class CpfJdbcDataSourceRegistryTest {

    @Test
    void neverCollapsesSoleBusinessDataSourceIntoPlatformRole() {
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        beans.registerSingleton("cpfDomainDataSource", mock(DataSource.class));

        CpfJdbcDataSourceRegistry registry = new CpfJdbcDataSourceRegistry(beans, new MockEnvironment());

        assertThatThrownBy(() -> registry.require(CpfDatabaseRole.CPF_PLATFORM_DB))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("CPF DataSource role is not mapped");
    }

    @Test
    void resolvesConventionalPlatformRoleWithoutUsingBusinessCandidate() {
        DefaultListableBeanFactory beans = new DefaultListableBeanFactory();
        DataSource platform = mock(DataSource.class);
        beans.registerSingleton("cpfDomainDataSource", mock(DataSource.class));
        beans.registerSingleton("cpfPlatformDataSource", platform);

        CpfJdbcDataSourceRegistry registry = new CpfJdbcDataSourceRegistry(beans, new MockEnvironment());

        assertThat(registry.require(CpfDatabaseRole.CPF_PLATFORM_DB)).isSameAs(platform);
    }
}
