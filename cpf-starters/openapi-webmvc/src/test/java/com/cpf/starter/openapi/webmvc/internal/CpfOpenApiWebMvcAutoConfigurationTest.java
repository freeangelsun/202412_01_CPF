package com.cpf.starter.openapi.webmvc.internal;

import static org.assertj.core.api.Assertions.assertThat;
import com.cpf.starter.openapi.webmvc.api.CpfOpenApiOperations;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

class CpfOpenApiWebMvcAutoConfigurationTest {
    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(CpfOpenApiWebMvcAutoConfiguration.class))
            .withUserConfiguration(TestConfiguration.class);

    @Test void backsOffWhenDisabled() {
        runner.withPropertyValues("cpf.openapi.webmvc.enabled=false")
                .run(context -> assertThat(context).doesNotHaveBean(CpfOpenApiOperations.class));
    }

    @Test void createsOperationsWithSafeApiDocsDefault() {
        runner.run(context -> {
            assertThat(context).hasSingleBean(CpfOpenApiOperations.class);
            assertThat(context.getBean(CpfOpenApiWebMvcProperties.class).isApiDocsEnabled()).isFalse();
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class TestConfiguration {
        @Bean RequestMappingHandlerMapping requestMappingHandlerMapping() { return new RequestMappingHandlerMapping(); }
    }
}
