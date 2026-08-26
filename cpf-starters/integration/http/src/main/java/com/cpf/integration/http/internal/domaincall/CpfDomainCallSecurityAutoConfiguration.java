package com.cpf.integration.http.internal.domaincall;

import com.cpf.web.context.CpfHttpIngressTrust;
import com.cpf.web.context.CpfHttpIngressTrustResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

/**
 * Security boundary for CPF's machine-only Remote Domain transport.
 *
 * <p>The caller Header is never used as authentication. The canonical ingress resolver accepts
 * only an identity installed by a verified security/mTLS filter or an explicit operator-owned
 * peer mapping. Header6 and the resolved operation contract remain independently enforced by
 * {@link CpfDomainInvocationGuard} immediately before invocation.</p>
 */
@AutoConfiguration(afterName = "com.cpf.web.runtime.CpfWebContextAutoConfiguration")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({SecurityFilterChain.class, HttpSecurity.class})
public class CpfDomainCallSecurityAutoConfiguration {
    @Bean(name = "cpfDomainCallSecurityFilterChain")
    @Order(40)
    @ConditionalOnMissingBean(name = "cpfDomainCallSecurityFilterChain")
    SecurityFilterChain cpfDomainCallSecurityFilterChain(
            HttpSecurity http, CpfHttpIngressTrustResolver ingressTrustResolver) throws Exception {
        http.securityMatcher("/_cpf/domain/**")
                .authorizeHttpRequests(authorize -> authorize.anyRequest().access((authentication, context) ->
                        new AuthorizationDecision(ingressTrustResolver.resolve(context.getRequest()).trust()
                                == CpfHttpIngressTrust.TRUSTED_INTERNAL)))
                // This is a stateless machine endpoint; browser CSRF credentials are neither accepted nor required.
                .csrf(value -> value.disable())
                .requestCache(value -> value.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .httpBasic(value -> value.disable())
                .formLogin(value -> value.disable())
                .logout(value -> value.disable())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.FORBIDDEN))
                        .accessDeniedHandler((request, response, failure) ->
                                response.sendError(HttpStatus.FORBIDDEN.value())));
        return http.build();
    }
}
