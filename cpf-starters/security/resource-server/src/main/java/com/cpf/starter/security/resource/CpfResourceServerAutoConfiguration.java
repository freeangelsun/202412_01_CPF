package com.cpf.starter.security.resource;

import java.util.Set;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.core.context.SecurityContextHolder;

@AutoConfiguration
@EnableConfigurationProperties(CpfResourceServerProperties.class)
@ConditionalOnProperty(prefix = "cpf.security.resource-server", name = "enabled", havingValue = "true")
public class CpfResourceServerAutoConfiguration {
    @Bean
    JwtDecoder cpfJwtDecoder(CpfResourceServerProperties properties) {
        properties.validate();
        JwtDecoder decoder;
        if (properties.getIssuerUri() != null && !properties.getIssuerUri().isBlank()) {
            decoder = JwtDecoders.fromIssuerLocation(properties.getIssuerUri());
        } else {
            decoder = NimbusJwtDecoder.withJwkSetUri(properties.getJwkSetUri()).build();
        }
        if (decoder instanceof NimbusJwtDecoder nimbus) {
            var base = properties.getIssuerUri() == null || properties.getIssuerUri().isBlank()
                    ? JwtValidators.createDefault()
                    : JwtValidators.createDefaultWithIssuer(properties.getIssuerUri());
            nimbus.setJwtValidator(new DelegatingOAuth2TokenValidator<>(base,
                    new CpfAudienceValidator(Set.copyOf(properties.getAudiences()))));
        }
        return decoder;
    }

    @Bean
    SecurityFilterChain cpfResourceServerSecurityFilterChain(HttpSecurity http, CpfResourceServerProperties properties) throws Exception {
        properties.validate();
        String[] publicPaths = properties.getPublicPaths().toArray(String[]::new);
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.requestMatchers(publicPaths).permitAll().anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));
        return http.build();
    }

    @Bean
    CpfSecurityContext cpfSecurityContext(CpfResourceServerProperties properties) {
        return new CpfSecurityContext(properties, SecurityContextHolder::getContext);
    }

    @Bean("cpfResourceServerHealthIndicator")
    HealthIndicator cpfResourceServerHealthIndicator(CpfResourceServerProperties properties) {
        return () -> Health.unknown()
                .withDetail("configured", true)
                .withDetail("mode", properties.getIssuerUri() != null ? "issuer" : "jwk-set")
                .withDetail("reason", "configuration-present; token endpoint/JWK reachability is not asserted by this indicator")
                .build();
    }
}
