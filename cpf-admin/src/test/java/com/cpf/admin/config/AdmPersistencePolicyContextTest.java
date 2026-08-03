package com.cpf.admin.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class AdmPersistencePolicyContextTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(AdmPersistencePolicy.class);

    @Test
    void productDefaultRequiresDatabaseAndNeverInventsMemoryPersistence() {
        runner.run(context -> {
            assertThat(context).hasNotFailed();
            AdmPersistencePolicy policy = context.getBean(AdmPersistencePolicy.class);
            assertThat(policy.databaseRequired()).isTrue();
            assertThat(policy.memoryEnabled()).isFalse();
            assertThat(context).doesNotHaveBean(javax.sql.DataSource.class);
        });
    }

    @Test
    void memoryModeWithoutEduOrTestProfileFailsClosed() {
        runner.withPropertyValues("cpf.adm.persistence.mode=memory")
                .run(context -> assertThat(context.getStartupFailure()).isNotNull());
    }

    @Test
    void explicitTestProfileMayUseMemoryWithoutJdbcBeans() {
        runner.withPropertyValues(
                        "spring.profiles.active=test",
                        "cpf.adm.persistence.mode=memory")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(AdmPersistencePolicy.class).memoryEnabled()).isTrue();
                    assertThat(context).doesNotHaveBean(javax.sql.DataSource.class);
                });
    }
}
