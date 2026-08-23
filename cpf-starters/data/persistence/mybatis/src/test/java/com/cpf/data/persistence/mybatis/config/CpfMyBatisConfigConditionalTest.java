package com.cpf.data.persistence.mybatis.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class CpfMyBatisConfigConditionalTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CpfMyBatisConfig.class));

    @Test
    void platformMyBatisOwnerStaysAbsentWithoutItsNamedPlatformDataSource() {
        runner.run(context -> assertThat(context)
                .hasNotFailed()
                .doesNotHaveBean("cpfSqlSessionFactory")
                .doesNotHaveBean("cpfSqlSessionTemplate"));
    }
}
