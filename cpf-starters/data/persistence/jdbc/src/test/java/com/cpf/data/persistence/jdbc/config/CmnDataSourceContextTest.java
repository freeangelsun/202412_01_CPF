package com.cpf.data.persistence.jdbc.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CmnDataSourceContextTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(CmnDataSourceConfig.class);

    @Test
    void libraryRuntimeModeDoesNotCreateCmnJdbcBeans() {
        runner.withPropertyValues("cpf.common.runtime-mode=library")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean("cpfCommonDataSource");
                    assertThat(context).doesNotHaveBean("cpfCommonTransactionManager");
                });
    }

    @Test
    void productRuntimeWithoutDatasourceConfigurationFailsClosed() {
        runner.run(context -> assertThat(context.getStartupFailure()).isNotNull());
    }

    @Test
    void invalidConfiguredDatasourceNeverBecomesAFalseGreenBean() {
        runner.withPropertyValues(
                        "cpf.common.runtime-mode=product",
                        "spring.datasource.cmn.url=jdbc:invalid:test",
                        "spring.datasource.cmn.driver-class-name=java.lang.String")
                .run(context -> assertThat(context.getStartupFailure()).isNotNull());
    }
}
