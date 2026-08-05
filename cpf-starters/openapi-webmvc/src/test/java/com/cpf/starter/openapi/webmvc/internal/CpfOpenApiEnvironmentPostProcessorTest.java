package com.cpf.starter.openapi.webmvc.internal;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

class CpfOpenApiEnvironmentPostProcessorTest {
    @Test void disablesApiDocsAndSwaggerUiByDefault() {
        MockEnvironment environment = new MockEnvironment();
        new CpfOpenApiEnvironmentPostProcessor().postProcessEnvironment(environment, null);
        assertThat(environment.getProperty("springdoc.api-docs.enabled", Boolean.class)).isFalse();
        assertThat(environment.getProperty("springdoc.swagger-ui.enabled", Boolean.class)).isFalse();
    }

    @Test void explicitProductSettingWins() {
        MockEnvironment environment = new MockEnvironment().withProperty("springdoc.api-docs.enabled", "true");
        new CpfOpenApiEnvironmentPostProcessor().postProcessEnvironment(environment, null);
        assertThat(environment.getProperty("springdoc.api-docs.enabled", Boolean.class)).isTrue();
    }
}
