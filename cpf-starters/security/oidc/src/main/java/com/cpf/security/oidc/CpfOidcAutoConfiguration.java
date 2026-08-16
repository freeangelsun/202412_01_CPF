package com.cpf.security.oidc;

import java.time.Clock;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;

/** Standard OIDC Login/SSO with CPF identity/tenant context, safe security events and refresh/client-credentials support. */
@AutoConfiguration
@EnableConfigurationProperties(CpfOidcProperties.class)
@ConditionalOnClass({SecurityFilterChain.class,ClientRegistrationRepository.class})
@ConditionalOnProperty(prefix="cpf.security.oidc",name="enabled",havingValue="true")
public class CpfOidcAutoConfiguration {
 @Bean @ConditionalOnMissingBean CpfOidcPrincipalMapper cpfOidcPrincipalMapper(CpfOidcProperties p){return new CpfOidcPrincipalMapper(p);}
 @Bean @ConditionalOnMissingBean CpfOidcSecurityEventSink cpfOidcSecurityEventSink(){return CpfOidcSecurityEventSink.NOOP;}
 @Bean @ConditionalOnMissingBean CpfOidcUserService cpfOidcUserService(CpfOidcPrincipalMapper m,CpfOidcSecurityEventSink e){return new CpfOidcUserService(m,e);}
 @Bean @ConditionalOnMissingBean CpfOidcContext cpfOidcContext(CpfOidcPrincipalMapper m){return new CpfOidcContext(m);}
 @Bean @ConditionalOnMissingBean CpfOidcContextBridge cpfOidcContextBridge(){return new CpfOidcContextBridge();}
 @Bean @ConditionalOnMissingBean Clock cpfOidcClock(){return Clock.systemUTC();}
 @Bean FilterRegistrationBean<CpfOidcContextFilter> cpfOidcContextFilter(CpfOidcContext c,CpfOidcContextBridge b,Clock clock){var r=new FilterRegistrationBean<>(new CpfOidcContextFilter(c,b,clock));r.setName("cpfOidcContextFilter");r.setOrder(20);return r;}

 @Bean(name="cpfOidcSecurityFilterChain") @Order(80) @ConditionalOnBean(ClientRegistrationRepository.class)
 SecurityFilterChain cpfOidcSecurityFilterChain(HttpSecurity http,ClientRegistrationRepository registrations,CpfOidcUserService users,CpfOidcProperties properties,CpfOidcPrincipalMapper mapper,CpfOidcSecurityEventSink events) throws Exception {
   var delegate=new OidcClientInitiatedLogoutSuccessHandler(registrations);delegate.setPostLogoutRedirectUri(properties.getPostLogoutRedirectUri());
   http.securityMatcher("/oauth2/**","/login/**","/logout").authorizeHttpRequests(a->a.anyRequest().permitAll())
     .oauth2Login(login->login.userInfoEndpoint(user->user.oidcUserService(users)))
     .logout(value->value.logoutSuccessHandler((request,response,authentication)->{if(authentication!=null&&authentication.getPrincipal() instanceof OidcUser oidc){var p=mapper.map(oidc);var cpf=com.cpf.core.api.context.CpfContexts.snapshot();events.record("OIDC_LOGOUT",p.userId(),p.tenantId(),cpf==null?null:cpf.context().transactionId());}delegate.onLogoutSuccess(request,response,authentication);}));
   return http.build();
 }
 @Bean @ConditionalOnMissingBean @ConditionalOnBean(OAuth2AuthorizedClientService.class)
 OAuth2AuthorizedClientManager cpfOidcAuthorizedClientManager(ClientRegistrationRepository registrations,OAuth2AuthorizedClientService clients){var provider=OAuth2AuthorizedClientProviderBuilder.builder().authorizationCode().refreshToken().clientCredentials().build();var manager=new AuthorizedClientServiceOAuth2AuthorizedClientManager(registrations,clients);manager.setAuthorizedClientProvider(provider);return manager;}
}
