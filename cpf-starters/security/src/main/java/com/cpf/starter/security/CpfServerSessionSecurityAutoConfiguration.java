package com.cpf.starter.security;

import javax.sql.DataSource;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

/** ADM/BZA 토큰을 브라우저 JavaScript에 노출하지 않는 JDBC Session 보안 경계입니다. */
@AutoConfiguration
@ConditionalOnClass({DataSource.class, CookieSerializer.class})
@EnableConfigurationProperties(CpfServerSessionProperties.class)
public class CpfServerSessionSecurityAutoConfiguration {
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
    FilterRegistrationBean<CpfBffCsrfFilter> cpfBffCsrfFilter() {
        FilterRegistrationBean<CpfBffCsrfFilter> bean=new FilterRegistrationBean<>(new CpfBffCsrfFilter()); bean.setOrder(Ordered.HIGHEST_PRECEDENCE+10); return bean;
    }
    @Bean
    FilterRegistrationBean<CpfBffSessionBridgeFilter> cpfBffSessionBridgeFilter() {
        FilterRegistrationBean<CpfBffSessionBridgeFilter> bean=new FilterRegistrationBean<>(new CpfBffSessionBridgeFilter()); bean.setOrder(Ordered.HIGHEST_PRECEDENCE+20); return bean;
    }
    @Bean CpfBffCredentialResponseAdvice cpfBffCredentialResponseAdvice(){return new CpfBffCredentialResponseAdvice();}

    @Bean
    @ConditionalOnProperty(name = "cpf.security.session.fail-closed", matchIfMissing = true)
    CpfSessionReadinessVerifier cpfSessionReadinessVerifier(
            DataSource dataSource, Environment environment, CpfServerSessionProperties properties) {
        return new CpfSessionReadinessVerifier(dataSource, environment, properties);
    }
}
