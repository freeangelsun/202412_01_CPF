package com.cpf.starter.security.identity;
import java.time.Clock;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
@AutoConfiguration
@EnableConfigurationProperties(CpfServiceIdentityProperties.class)
@ConditionalOnProperty(prefix="cpf.security.service-identity",name="enabled",havingValue="true")
public class CpfServiceIdentityAutoConfiguration {
 @Bean CpfServiceIdentityTokenService cpfServiceIdentityTokenService(CpfServiceIdentityProperties p){p.validate();return new CpfServiceIdentityTokenService(p,Clock.systemUTC());}
 @Bean("cpfServiceIdentityHealthIndicator") HealthIndicator health(CpfServiceIdentityProperties p){return ()->Health.up().withDetail("serviceId",p.getServiceId()).withDetail("activeKeyId",p.getActiveKeyId()).build();}
}
