package com.cpf.backoffice.web.shared.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import com.cpf.backoffice.web.shared.config.BackofficeWebProperties;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

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
                // Spring Security 6 기본 handler 는 XorCsrfTokenRequestAttributeHandler 라
                // Header 값이 XOR 마스킹된 토큰이기를 기대한다. 그런데 이 채널 프론트의 SPA
                // (channelHttpClient.ts)는 표준 방식대로 XSRF-TOKEN Cookie 원본을 그대로
                // X-XSRF-TOKEN 으로 되돌려 보낸다. 기본 handler 를 두면 모든 상태 변경 요청이
                // 403 이 된다(ADM BFF Chain 에서 같은 결함을 이미 확인했다).
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers("/actuator/**"))
                .build();
    }
}
