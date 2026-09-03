package com.cpf.security.session.jdbc;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/**
 * ADM/MBW Browser Credential을 HttpOnly Session Handle + 암호화 JDBC Vault로 보호합니다.
 *
 * <p>{@code cpfBffCredentialObjectMapper}는 BFF 자격증명 전용 대체재이지 Platform의 canonical
 * ObjectMapper가 아닙니다. 이 AutoConfiguration이 먼저 처리되면 {@code @ConditionalOnMissingBean}
 * 경쟁에서 foundation의 CpfJackson2AutoConfiguration이 back-off하고, Security Session이 꺼진
 * Runtime이나 이른 시점 소비자에게 ObjectMapper가 없어집니다. foundation이 먼저 등록되도록
 * 순서를 명시하며, 모듈 의존을 늘리지 않기 위해 이름으로 지정합니다.</p>
 */
@AutoConfiguration(afterName = "com.cpf.starter.runtime.CpfJackson2AutoConfiguration")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({DataSource.class, SecurityFilterChain.class, CookieSerializer.class})
@ConditionalOnProperty(name = "cpf.security.session.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CpfServerSessionProperties.class)
public class CpfServerSessionSecurityAutoConfiguration {
    /** CPF 논리 Platform DB role의 canonical Bean 이름입니다. */
    static final String CPF_PLATFORM_DATA_SOURCE = "cpfPlatformDataSource";

    @Bean
    @ConditionalOnMissingBean(ObjectMapper.class)
    ObjectMapper cpfBffCredentialObjectMapper() {
        return new ObjectMapper().findAndRegisterModules();
    }

    @Bean
    @ConditionalOnMissingBean
    CookieSerializer cpfSessionCookieSerializer(CpfServerSessionProperties properties) {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName(properties.cookieName());
        serializer.setCookiePath(properties.cookiePath());
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(properties.secure());
        serializer.setSameSite(properties.sameSite());
        return serializer;
    }

    @Bean
    @ConditionalOnMissingBean(CpfBffCredentialVault.class)
    CpfBffCredentialVault cpfBffCredentialVault(
            ListableBeanFactory beanFactory,
            ObjectProvider<DataSource> dataSources,
            Environment environment,
            CpfServerSessionProperties properties) {
        boolean product = isProductProfile(environment);
        byte[] key = CpfSessionReadinessVerifier.decodeKey(properties.credentialKeyBase64(), product);
        return new JdbcCpfBffCredentialVault(
                new JdbcTemplate(resolveSessionDataSource(beanFactory, dataSources)), key, properties.credentialKeyId());
    }

    @Bean
    CookieCsrfTokenRepository cpfBffCsrfTokenRepository(CpfServerSessionProperties properties) {
        CookieCsrfTokenRepository repository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        repository.setCookieName("XSRF-TOKEN");
        repository.setHeaderName("X-XSRF-TOKEN");
        repository.setCookiePath(properties.cookiePath());
        repository.setCookieCustomizer(builder -> builder
                .secure(properties.secure())
                .sameSite(properties.sameSite()));
        return repository;
    }

    @Bean
    CpfBffSessionBridgeFilter cpfBffSessionBridgeFilter(CpfBffCredentialVault vault) {
        return new CpfBffSessionBridgeFilter(vault);
    }

    @Bean
    CpfBffLogoutFilter cpfBffLogoutFilter(CpfBffCredentialVault vault) {
        return new CpfBffLogoutFilter(vault);
    }

    @Bean
    CpfTrustedOriginFilter cpfTrustedOriginFilter(CpfServerSessionProperties properties) {
        return new CpfTrustedOriginFilter(properties.allowedOrigins());
    }

    @Bean
    FilterRegistrationBean<CpfTrustedOriginFilter> cpfTrustedOriginFilterRegistration(
            CpfTrustedOriginFilter filter) {
        return securityChainOnly(filter);
    }

    @Bean
    FilterRegistrationBean<CpfBffSessionBridgeFilter> cpfBffSessionBridgeFilterRegistration(
            CpfBffSessionBridgeFilter filter) {
        return securityChainOnly(filter);
    }

    @Bean
    FilterRegistrationBean<CpfBffLogoutFilter> cpfBffLogoutFilterRegistration(
            CpfBffLogoutFilter filter) {
        return securityChainOnly(filter);
    }

    @Bean(name = "cpfBffSecurityFilterChain")
    @Order(90)
    SecurityFilterChain cpfBffSecurityFilterChain(
            HttpSecurity http,
            CookieCsrfTokenRepository csrfRepository,
            CpfTrustedOriginFilter originFilter,
            CpfBffSessionBridgeFilter bridgeFilter,
            CpfBffLogoutFilter logoutFilter,
            CpfServerSessionProperties properties) throws Exception {
        http.securityMatcher("/adm/**")
                .authorizeHttpRequests(authorize -> authorize
                        // 정적 Shell과 최초 로그인만 공개합니다. 권한은 Browser 표시가 아니라 Server Chain이 소유합니다.
                        .requestMatchers(
                                "/adm", "/adm/", "/adm/index.html", "/adm/assets/**",
                                "/adm/api/auth/login")
                        .permitAll()
                        // Health/Liveness/Readiness 는 AdmApiAuthFilter.isPublicHealthRequest 가
                        // 이미 GET/HEAD 공개로 선언한 3개 경로다. 그런데 이 Chain 이 그 Filter 앞에서
                        // 401 로 잘라내면 "공개 선언" 이 실현되지 않는다. 실제로 1-WAS 에서
                        // GET /adm/api/health 가 401 이 되어 로그정책 검증기의 기동 재사용 판정이
                        // 실패하고 이미 떠 있는 Runtime 을 두고 ADM boot jar 를 다시 찾았다.
                        // 두 계층의 공개 범위를 같은 3개 경로/같은 read-only method 로 일치시킨다.
                        .requestMatchers(HttpMethod.GET,
                                "/adm/api/health", "/adm/api/health/liveness", "/adm/api/health/readiness")
                        .permitAll()
                        .requestMatchers(HttpMethod.HEAD,
                                "/adm/api/health", "/adm/api/health/liveness", "/adm/api/health/readiness")
                        .permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, failure) ->
                                response.sendError(HttpStatus.FORBIDDEN.value())))
                // Spring Security 6 의 기본 CsrfTokenRequestHandler 는
                // XorCsrfTokenRequestAttributeHandler 다. 이 handler 는 Header 값이 "XOR 마스킹된"
                // 토큰이기를 기대하는데, CookieCsrfTokenRepository 는 Cookie 에 **원본** 토큰을 저장한다.
                // ADM SPA(cpfApi.ts)와 Backoffice Web 은 표준 SPA 방식대로 XSRF-TOKEN Cookie 값을
                // 그대로 X-XSRF-TOKEN 으로 되돌려 보내므로, 기본 handler 로는 모든 상태 변경 요청이
                // 403 이 된다(실제로 1-WAS 에서 POST /adm/api/auth/login 이 403 으로 막혔다).
                // Cookie 기반 SPA 계약에 맞는 plain handler 를 명시한다. XOR(BREACH 완화)을 포기하는
                // 대신, 같은 Chain 의 CpfTrustedOriginFilter 가 Origin/Referer allowlist 로
                // cross-origin 상태 변경을 이미 차단한다.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(csrfRepository)
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler()))
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                        .sessionFixation(fixation -> fixation.changeSessionId()))
                .requestCache(cache -> cache.disable())
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())
                .headers(headers -> headers
                        .contentTypeOptions(Customizer.withDefaults())
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(hsts -> hsts.includeSubDomains(true).preload(true))
                        .referrerPolicy(referrer -> referrer.policy(
                                ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER))
                        .contentSecurityPolicy(csp -> csp.policyDirectives(properties.contentSecurityPolicy()))
                        .permissionsPolicyHeader(policy -> policy.policy(
                                "camera=(), microphone=(), geolocation=(), payment=()")))
                .addFilterBefore(originFilter, CsrfFilter.class)
                .addFilterAfter(new CpfCsrfCookieExposureFilter(), CsrfFilter.class)
                .addFilterAfter(bridgeFilter, CpfCsrfCookieExposureFilter.class)
                .addFilterAfter(logoutFilter, CpfBffSessionBridgeFilter.class);
        return http.build();
    }

    // 서버 세션 저장소(FindByIndexNameSessionRepository)는 Boot 의 spring-boot-session-jdbc
    // 자동설정이 소유한다. 한때 이 클래스가 JdbcIndexedSessionRepository 를 직접 만들었는데,
    // 그것은 Boot 4 에서 session 자동설정이 **별도 모듈로 분리**된 것을 "자동설정 부재"로 오인한
    // 결과였다. 조립 누락은 Starter 선언으로 닫는다(build.gradle 참조).
    // 저장 테이블 SPRING_SESSION / SPRING_SESSION_ATTRIBUTES 는 CPF 정본 스키마가 소유하며
    // Spring Session 표준 DDL 과 동일하다. Boot 의 initialize-schema 기본값은 embedded 라
    // 공식 3개 벤더에서는 Runtime DDL 을 만들지 않는다.

    @Bean
    CpfBffConcurrentSessionController cpfBffConcurrentSessionController(
            FindByIndexNameSessionRepository<? extends Session> sessionRepository) {
        return new CpfBffConcurrentSessionController(sessionRepository);
    }

    @Bean
    CpfBffCredentialResponseAdvice cpfBffCredentialResponseAdvice(
            CpfBffCredentialVault vault,
            CpfServerSessionProperties properties,
            ObjectMapper mapper,
            CpfBffConcurrentSessionController concurrentSessions) {
        return new CpfBffCredentialResponseAdvice(vault, properties, mapper, concurrentSessions);
    }

    @Bean
    CpfBffSessionDestroyedListener cpfBffSessionDestroyedListener(CpfBffCredentialVault vault) {
        return new CpfBffSessionDestroyedListener(vault);
    }

    private static <T extends jakarta.servlet.Filter> FilterRegistrationBean<T> securityChainOnly(T filter) {
        FilterRegistrationBean<T> registration = new FilterRegistrationBean<>(filter);
        registration.setEnabled(false);
        return registration;
    }

    @Bean
    @ConditionalOnProperty(name = "cpf.security.session.fail-closed", matchIfMissing = true)
    CpfSessionReadinessVerifier cpfSessionReadinessVerifier(
            ListableBeanFactory beanFactory,
            ObjectProvider<DataSource> dataSources,
            Environment environment,
            CpfServerSessionProperties properties) {
        return new CpfSessionReadinessVerifier(resolveSessionDataSource(beanFactory, dataSources), environment, properties);
    }

    /**
     * 세션/BFF 자격증명은 CPF Platform 스키마(SEC_BFF_CREDENTIAL_VAULT)가 소유합니다.
     *
     * <p>One-WAS 통합 실행처럼 ADM/Backoffice/Common/Platform/Customer DataSource가 함께 있는
     * 구성에서 무한정 {@code DataSource} 주입은 후보가 여러 개라 기동 자체를 실패시킵니다.
     * 그래서 논리 role 관례 Bean 이름을 우선 사용하고, DataSource가 하나뿐인 단일 앱 구성에서는
     * 그 하나를 사용합니다. 어느 쪽도 정할 수 없으면 조용히 임의 선택하지 않고 fail-closed 합니다.</p>
     */
    static DataSource resolveSessionDataSource(
            ListableBeanFactory beanFactory, ObjectProvider<DataSource> dataSources) {
        if (beanFactory.containsBean(CPF_PLATFORM_DATA_SOURCE)
                && beanFactory.isTypeMatch(CPF_PLATFORM_DATA_SOURCE, DataSource.class)) {
            return beanFactory.getBean(CPF_PLATFORM_DATA_SOURCE, DataSource.class);
        }
        DataSource unique = dataSources.getIfUnique();
        if (unique != null) {
            return unique;
        }
        throw new IllegalStateException(
                "CPF session/BFF credential storage requires the CPF Platform DataSource. "
                        + "Enable the '" + CPF_PLATFORM_DATA_SOURCE + "' role bean "
                        + "(cpf.data.persistence.jdbc.role-datasources.cpf-platform-db.enabled=true) "
                        + "when more than one DataSource is present.");
    }

    static boolean isProductProfile(Environment environment) {
        return environment.matchesProfiles("prod", "stg", "qa", "dr");
    }
}
