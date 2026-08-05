package com.cpf.starter.openapi.webmvc.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/** Applies secure defaults before Springdoc auto-configuration without overriding explicit product settings. */
public final class CpfOpenApiEnvironmentPostProcessor implements EnvironmentPostProcessor {
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        Map<String, Object> defaults = new LinkedHashMap<>();
        if (!environment.containsProperty("springdoc.api-docs.enabled")) {
            defaults.put("springdoc.api-docs.enabled",
                    environment.getProperty("cpf.openapi.webmvc.api-docs-enabled", Boolean.class, false));
        }
        if (!environment.containsProperty("springdoc.swagger-ui.enabled")) {
            defaults.put("springdoc.swagger-ui.enabled", false);
        }
        if (!environment.containsProperty("springdoc.api-docs.path")) {
            defaults.put("springdoc.api-docs.path",
                    environment.getProperty("cpf.openapi.webmvc.api-docs-path", "/v3/api-docs"));
        }
        if (!defaults.isEmpty()) environment.getPropertySources().addLast(new MapPropertySource("cpfOpenApiSecureDefaults", defaults));
    }
}
