package com.cpf.backoffice.online.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * MBW 업무 Domain Runtime 의 HTTP 보안 경계를 선언합니다.
 *
 * <p>증상 근거: 공개 배포본에서 MBW 를 단독 기동하면 모든 요청이 본문 없는 401 로 거절됐다.
 * 최초 로그인조차 불가능해 "토큰을 얻으려면 토큰이 필요한" 상태였다.</p>
 *
 * <p>원인: Spring Security 가 classpath 에 있는데 이 Runtime 에 SecurityFilterChain 선언이 없어
 * Spring Boot 기본 체인이 모든 경로를 막았다. 그 결과 MBW 의 인가 주체인
 * {@code BackofficeApiAuthFilter} 는 실행될 기회조차 얻지 못했다. 통합 Runtime 에서는 다른 모듈이
 * 체인을 선언했기 때문에 이 결함이 드러나지 않았다.</p>
 *
 * <p>이 Runtime 의 인가 정본은 {@code BackofficeApiAuthFilter} 다. 그 필터가
 * {@code /api/v1/backoffice/**} 를 강제하고 인증 진입점({@code /api/v1/backoffice/auth/**})만 연다.
 * CSRF/세션 쿠키 경계는 Channel Front(Backoffice Web)가 소유하며 이 Runtime 은 token 기반 API 다.</p>
 *
 * <p>되돌리면 재발할 증상: MBW 단독 기동에서 모든 API 가 401 이 되어 업무 거래가 성립하지 않는다.</p>
 */
@Configuration(proxyBeanMethods = false)
public class BackofficeSecurityConfiguration {

    @Bean
    SecurityFilterChain backofficeApiSecurityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
