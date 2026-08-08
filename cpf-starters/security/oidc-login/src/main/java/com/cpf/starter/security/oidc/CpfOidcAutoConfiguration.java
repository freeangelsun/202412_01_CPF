package com.cpf.starter.security.oidc;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 표준 OIDC Login을 CPF BFF/Servlet Session과 함께 사용할 수 있도록 최소 설정으로 연결합니다.
 * Provider 등록은 표준 spring.security.oauth2.client.registration/provider 설정을 그대로 사용합니다.
 */
@AutoConfiguration
@EnableConfigurationProperties(CpfOidcProperties.class)
@ConditionalOnClass({SecurityFilterChain.class, ClientRegistrationRepository.class})
@ConditionalOnProperty(prefix="cpf.security.oidc", name="enabled", havingValue="true")
public class CpfOidcAutoConfiguration {
    @Bean @ConditionalOnMissingBean
    CpfOidcPrincipalMapper cpfOidcPrincipalMapper(CpfOidcProperties properties) { return new CpfOidcPrincipalMapper(properties); }

    @Bean @ConditionalOnMissingBean
    CpfOidcUserService cpfOidcUserService(CpfOidcPrincipalMapper mapper) { return new CpfOidcUserService(mapper); }

    @Bean @ConditionalOnMissingBean
    CpfOidcContext cpfOidcContext(CpfOidcPrincipalMapper mapper) { return new CpfOidcContext(mapper); }

    @Bean(name = "cpfOidcSecurityFilterChain")
    @Order(80)
    @ConditionalOnBean(ClientRegistrationRepository.class)
    SecurityFilterChain cpfOidcSecurityFilterChain(HttpSecurity http, ClientRegistrationRepository registrations,
            CpfOidcUserService users, CpfOidcProperties properties) throws Exception {
        var logout = new OidcClientInitiatedLogoutSuccessHandler(registrations);
        logout.setPostLogoutRedirectUri(properties.getPostLogoutRedirectUri());
        http.securityMatcher("/oauth2/**", "/login/**", "/logout")
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .oauth2Login(login -> login.userInfoEndpoint(user -> user.oidcUserService(users)))
                .logout(value -> value.logoutSuccessHandler(logout));
        return http.build();
    }

    @Bean @ConditionalOnMissingBean
    @ConditionalOnBean(OAuth2AuthorizedClientService.class)
    OAuth2AuthorizedClientManager cpfOidcAuthorizedClientManager(
            ClientRegistrationRepository registrations,
            OAuth2AuthorizedClientService clients) {
        var provider = OAuth2AuthorizedClientProviderBuilder.builder()
                .authorizationCode().refreshToken().clientCredentials().build();
        var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations, clients);
        manager.setAuthorizedClientProvider(provider);
        return manager;
    }
}
