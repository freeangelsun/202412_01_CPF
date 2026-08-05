package com.cpf.starter.openapi.webmvc.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.web.util.pattern.PathPatternParser;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

class DefaultCpfOpenApiOperationsTest {
    @Test
    void inventoriesLegacyRoutesAndRejectsUnauditedRefresh() {
        RequestMappingHandlerMapping mappings = mappings(RequestMappingInfo.paths("/api/test").build());
        CpfOpenApiWebMvcProperties properties = new CpfOpenApiWebMvcProperties();
        properties.setApiDocsEnabled(true);
        DefaultCpfOpenApiOperations operations = new DefaultCpfOpenApiOperations(properties, mappings,
                Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC));

        assertThat(operations.refresh("startup verification").operationCount()).isEqualTo(1);
        assertThat(operations.snapshot().apiDocsEnabled()).isTrue();
        assertThatThrownBy(() -> operations.refresh(" ")).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void inventoriesParsedPathPatternRoutes() {
        RequestMappingInfo.BuilderConfiguration options = new RequestMappingInfo.BuilderConfiguration();
        options.setPatternParser(new PathPatternParser());
        RequestMappingInfo parsed = RequestMappingInfo.paths("/api/parsed/{id}").options(options).build();
        DefaultCpfOpenApiOperations operations = new DefaultCpfOpenApiOperations(new CpfOpenApiWebMvcProperties(),
                mappings(parsed), Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC));

        assertThat(operations.refresh("parsed path verification").operationCount()).isEqualTo(1);
        assertThat(operations.snapshot().status()).isEqualTo(com.cpf.starter.openapi.webmvc.api.CpfOpenApiStatus.UP);
    }

    private static RequestMappingHandlerMapping mappings(RequestMappingInfo mappingInfo) {
        RequestMappingHandlerMapping mappings = mock(RequestMappingHandlerMapping.class);
        when(mappings.getHandlerMethods()).thenReturn(Map.of(mappingInfo,
                new org.springframework.web.method.HandlerMethod(new TestController(), "test")));
        return mappings;
    }

    static final class TestController { public void test() {} }
}
