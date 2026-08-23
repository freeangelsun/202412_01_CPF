package com.cpf.starter.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class CpfJackson2AutoConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CpfJackson2AutoConfiguration.class));

    @Test
    void providesModuleAwareJackson2Mapper() {
        runner.run(context -> {
            assertThat(context).hasNotFailed().hasSingleBean(ObjectMapper.class);
            ObjectMapper mapper = context.getBean(ObjectMapper.class);
            LocalDate expected = LocalDate.of(2026, 8, 23);
            assertThat(mapper.readValue(mapper.writeValueAsString(expected), LocalDate.class))
                    .isEqualTo(expected);
        });
    }

    @Test
    void preservesDeveloperProvidedJackson2Mapper() {
        ObjectMapper custom = new ObjectMapper();
        runner.withBean("customJackson2ObjectMapper", ObjectMapper.class, () -> custom)
                .run(context -> {
                    assertThat(context).hasNotFailed().hasSingleBean(ObjectMapper.class);
                    assertThat(context.getBean(ObjectMapper.class)).isSameAs(custom);
                });
    }
}
