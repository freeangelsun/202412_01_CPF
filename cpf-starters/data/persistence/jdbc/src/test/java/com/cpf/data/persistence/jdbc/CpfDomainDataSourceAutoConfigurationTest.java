package com.cpf.data.persistence.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class CpfDomainDataSourceAutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    CpfDomainDataSourceAutoConfiguration.class,
                    DataSourceAutoConfiguration.class));

    @Test
    void generatedDomainOwnsTheConfiguredBusinessDataSourceWithoutBootDuplicate() {
        runner.withPropertyValues(
                        "cpf.domain.persistence.enabled=true",
                        "cpf.domain.persistence.required=true",
                        "cpf.domain.persistence.provider=mybatis",
                        "cpf.domain.persistence.data-source-prefix=spring.datasource",
                        "spring.datasource.url=jdbc:cpf-test:business")
                .run(context -> assertThat(context)
                        .hasNotFailed()
                        .hasBean("cpfDomainDataSource")
                        .hasBean("cpfDomainTransactionManager")
                        .doesNotHaveBean("dataSource"));
    }

    @Test
    void persistenceNoneDoesNotCreateDomainDatabaseBeans() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(CpfDomainDataSourceAutoConfiguration.class))
                .run(context -> assertThat(context)
                .hasNotFailed()
                .doesNotHaveBean("cpfDomainDataSource")
                .doesNotHaveBean("cpfDomainTransactionManager"));
    }
}
