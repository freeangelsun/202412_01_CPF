package com.cpf.batch.control.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;

@Configuration
public class BatControlSecurityConfiguration {
    @Bean
    BatControlAuthenticationFilter batControlAuthenticationFilter(Environment environment) {
        return new BatControlAuthenticationFilter(environment);
    }

    @Bean
    FilterRegistrationBean<BatControlAuthenticationFilter> disableContainerRegistration(
            BatControlAuthenticationFilter authenticationFilter) {
        FilterRegistrationBean<BatControlAuthenticationFilter> registration =
                new FilterRegistrationBean<>(authenticationFilter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    SecurityFilterChain batControlSecurityFilterChain(
            HttpSecurity http,
            BatControlAuthenticationFilter authenticationFilter) throws Exception {
        http
                .csrf(value -> value.disable())
                .httpBasic(value -> value.disable())
                .formLogin(value -> value.disable())
                .logout(value -> value.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers("/bat/internal/**").hasAuthority("BAT_CALLER_ADM")
                        .requestMatchers(
                                "/api/v1/batch/runtime/registrations",
                                "/api/v1/batch/runtime/heartbeats",
                                "/api/v1/batch/job-packs/registrations")
                        .hasAuthority("BAT_RUNTIME")
                        .requestMatchers(
                                HttpMethod.POST,
                                "/api/v1/batch/**",
                                "/bat/api/**")
                        .hasAuthority("BAT_CALLER_ADM")
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, failure) ->
                                response.sendError(
                                        HttpServletResponse.SC_UNAUTHORIZED,
                                        "BAT authentication is required"))
                        .accessDeniedHandler((request, response, failure) ->
                                response.sendError(
                                        HttpServletResponse.SC_FORBIDDEN,
                                        "BAT caller is not authorized")))
                .addFilterBefore(authenticationFilter, AnonymousAuthenticationFilter.class);
        return http.build();
    }
}
