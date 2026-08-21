package com.cpf.backoffice.web.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import com.cpf.backoffice.web.shared.config.BackofficeWebProperties;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Browser 인증 Cookie와 업무 API의 CSRF 경계를 소유하는 Backoffice Web 보안 설정입니다.
 *
 * <p>업무 권한은 MBW Domain이 최종 판정하고, 이 계층은 Browser same-origin/CSRF와 BFF Endpoint 접근 경계를
 * 담당합니다. 인증용 Access/Refresh Cookie는 HttpOnly이고 CSRF token만 JavaScript가 읽을 수 있습니다.</p>
 */
@Configuration
public class BackofficeWebSecurityConfiguration {
    @Bean
    SecurityFilterChain backofficeWebSecurityFilterChain(HttpSecurity http, BackofficeWebProperties properties) throws Exception {
        CookieCsrfTokenRepository csrfRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfRepository.setCookiePath("/");
        return http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/actuator/health/**", "/actuator/info", "/assets/**", "/favicon.ico", "/api/v1/backoffice/auth/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new BackofficeCookieAuthenticationFilter(properties), AnonymousAuthenticationFilter.class)
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepository)
                        .ignoringRequestMatchers("/actuator/**"))
                .build();
    }
}
