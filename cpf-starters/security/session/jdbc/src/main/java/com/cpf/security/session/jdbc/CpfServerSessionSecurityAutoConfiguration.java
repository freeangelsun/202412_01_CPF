package com.cpf.security.session.jdbc;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.sql.DataSource;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/** ADM/BZA Browser Credential을 HttpOnly Session Handle + 암호화 JDBC Vault로 보호합니다. */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass({DataSource.class, SecurityFilterChain.class, CookieSerializer.class})
@ConditionalOnProperty(name = "cpf.security.session.enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(CpfServerSessionProperties.class)
public class CpfServerSessionSecurityAutoConfiguration {
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
            DataSource dataSource,
            Environment environment,
            CpfServerSessionProperties properties) {
        boolean product = isProductProfile(environment);
        byte[] key = CpfSessionReadinessVerifier.decodeKey(properties.credentialKeyBase64(), product);
        return new JdbcCpfBffCredentialVault(
                new JdbcTemplate(dataSource), key, properties.credentialKeyId());
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
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                        .accessDeniedHandler((request, response, failure) ->
                                response.sendError(HttpStatus.FORBIDDEN.value())))
                .csrf(csrf -> csrf.csrfTokenRepository(csrfRepository))
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
            DataSource dataSource,
            Environment environment,
            CpfServerSessionProperties properties) {
        return new CpfSessionReadinessVerifier(dataSource, environment, properties);
    }

    static boolean isProductProfile(Environment environment) {
        return environment.matchesProfiles("prod", "stg", "qa", "dr");
    }
}
