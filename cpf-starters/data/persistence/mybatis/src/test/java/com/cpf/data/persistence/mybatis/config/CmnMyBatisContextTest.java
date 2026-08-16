package com.cpf.data.persistence.mybatis.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class CmnMyBatisContextTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(CmnMyBatisConfig.class);

    @Test
    void libraryRuntimeModeDoesNotCreateCmnMyBatisBeans() {
        runner.withPropertyValues("cpf.common.runtime-mode=library")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean("cmnSqlSessionFactory");
                    assertThat(context).doesNotHaveBean("cmnSqlSessionTemplate");
                });
    }

    @Test
    void productRuntimeWithoutCmnDatasourceFailsClosed() {
        runner.run(context -> assertThat(context.getStartupFailure()).isNotNull());
    }
}
