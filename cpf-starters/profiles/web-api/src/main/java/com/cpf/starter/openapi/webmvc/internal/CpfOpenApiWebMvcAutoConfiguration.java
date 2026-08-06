package com.cpf.starter.openapi.webmvc.internal;

import com.cpf.starter.openapi.webmvc.api.CpfOpenApiContributor;
import com.cpf.starter.openapi.webmvc.api.CpfOpenApiOperations;
import io.swagger.v3.oas.models.OpenAPI;
import java.time.Clock;
import java.util.List;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({OpenAPI.class, RequestMappingHandlerMapping.class})
@ConditionalOnProperty(prefix = "cpf.openapi.webmvc", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CpfOpenApiWebMvcProperties.class)
public class CpfOpenApiWebMvcAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    Clock cpfOpenApiClock() { return Clock.systemUTC(); }

    @Bean @ConditionalOnMissingBean(CpfOpenApiOperations.class)
    CpfOpenApiOperations cpfOpenApiOperations(CpfOpenApiWebMvcProperties properties,
                                               RequestMappingHandlerMapping mappings,
                                               Clock cpfOpenApiClock) {
        return new DefaultCpfOpenApiOperations(properties, mappings, cpfOpenApiClock);
    }

    @Bean
    @ConditionalOnProperty(prefix = "cpf.openapi.webmvc", name = "api-docs-enabled", havingValue = "true")
    OpenApiCustomizer cpfOpenApiCustomizer(CpfOpenApiWebMvcProperties properties,
                                            ObjectProvider<CpfOpenApiContributor> contributors) {
        return new CpfOpenApiCustomizerAdapter(properties, contributors.orderedStream().toList());
    }

    @Bean(name = "cpfOpenApiHealthIndicator") @ConditionalOnMissingBean(name = "cpfOpenApiHealthIndicator")
    HealthIndicator cpfOpenApiHealthIndicator(CpfOpenApiOperations operations) {
        return new CpfOpenApiHealthIndicator(operations);
    }

    @Bean @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "cpf.openapi.webmvc", name = "management-enabled", havingValue = "true", matchIfMissing = true)
    CpfOpenApiActuatorEndpoint cpfOpenApiActuatorEndpoint(CpfOpenApiOperations operations) {
        return new CpfOpenApiActuatorEndpoint(operations);
    }
}
