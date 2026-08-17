package com.cpf.security;

import static org.assertj.core.api.Assertions.assertThat;

import com.cpf.security.api.password.CpfPasswordEncoder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class CpfPasswordAutoConfigurationTest {
    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CpfPasswordAutoConfiguration.class));

    @Test
    void assemblesPublicPasswordServiceWithoutApplicationComponentScan() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(CpfPasswordEncoder.class);
            assertThat(context.getBean(CpfPasswordEncoder.class).algorithmId()).isEqualTo("pbkdf2-sha256");
        });
    }

    @Test
    void productionProfileFailsClosedWhenPepperIsMissing() {
        contextRunner.withPropertyValues("spring.profiles.active=prod")
                .run(context -> assertThat(context).hasFailed());
    }
}
