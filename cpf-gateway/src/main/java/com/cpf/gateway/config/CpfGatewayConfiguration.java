package com.cpf.gateway.config;

import com.cpf.core.api.gateway.CpfGatewayAuditPort;
import com.cpf.core.api.gateway.CpfGatewayAuthenticationPort;
import com.cpf.core.api.gateway.CpfGatewayAuthorizationPort;
import com.cpf.core.api.gateway.CpfGatewayPrincipal;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.gateway.route.CpfGatewayRouteSnapshot;
import com.cpf.gateway.runtime.CpfGatewayRouteRuntimeApplier;
import com.cpf.gateway.runtime.CpfGatewayRuntimeApplier;
import com.cpf.gateway.runtime.CpfGatewayRuntimePolicy;
import com.cpf.gateway.runtime.CpfApiClientSecurityPolicy;
import com.cpf.gateway.runtime.CpfApiClientSecurityRuntimeApplier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestClient;

/** CPF Gateway runtime의 data plane Bean을 구성합니다. */
@Configuration
@EnableScheduling
public class CpfGatewayConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public CpfApiClientSecurityPolicy cpfApiClientSecurityPolicy() { return new CpfApiClientSecurityPolicy(); }

    @Bean(name = "cpfApiClientSecurityRuntimeApplier")
    @ConditionalOnMissingBean(name = "cpfApiClientSecurityRuntimeApplier")
    public CpfRuntimeChangeApplier cpfApiClientSecurityRuntimeApplier(CpfApiClientSecurityPolicy policy) {
        return new CpfApiClientSecurityRuntimeApplier(policy);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfGatewayRuntimePolicy cpfGatewayRuntimePolicy() { return new CpfGatewayRuntimePolicy(); }

    @Bean(name = "cpfGatewayRouteRuntimeApplier")
    @ConditionalOnMissingBean(name = "cpfGatewayRouteRuntimeApplier")
    public CpfRuntimeChangeApplier cpfGatewayRouteRuntimeApplier(CpfGatewayRouteSnapshot snapshot) {
        return new CpfGatewayRouteRuntimeApplier(snapshot);
    }

    @Bean(name = "cpfGatewayHeaderRuntimeApplier")
    @ConditionalOnMissingBean(name = "cpfGatewayHeaderRuntimeApplier")
    public CpfRuntimeChangeApplier cpfGatewayHeaderRuntimeApplier(CpfGatewayRuntimePolicy policy) {
        return new CpfGatewayRuntimeApplier("GATEWAY_HEADER", policy);
    }

    @Bean(name = "cpfGatewayCorsRuntimeApplier")
    @ConditionalOnMissingBean(name = "cpfGatewayCorsRuntimeApplier")
    public CpfRuntimeChangeApplier cpfGatewayCorsRuntimeApplier(CpfGatewayRuntimePolicy policy) {
        return new CpfGatewayRuntimeApplier("GATEWAY_CORS", policy);
    }

    @Bean(name = "cpfGatewayRateLimitRuntimeApplier")
    @ConditionalOnMissingBean(name = "cpfGatewayRateLimitRuntimeApplier")
    public CpfRuntimeChangeApplier cpfGatewayRateLimitRuntimeApplier(CpfGatewayRuntimePolicy policy) {
        return new CpfGatewayRuntimeApplier("RATE_LIMIT", policy);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfGatewayAuthenticationPort cpfGatewayAuthenticationPort(CpfApiClientSecurityPolicy policy) {
        // 기본 Adapter는 hash/IP/cert/expiry/quota가 검증된 API Client만 Principal로 승격합니다.
        return (route, credentials) -> policy.authenticate(
                credentials.get(com.cpf.core.api.header.CpfHeaderNames.API_KEY),
                credentials.get("cpf.client.ip"), credentials.get("cpf.client.cert.serial"));
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfGatewayAuthorizationPort cpfGatewayAuthorizationPort() {
        return (route, trustedHeaders) -> route.requiredPermission() == null || route.requiredPermission().isBlank();
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfGatewayAuditPort cpfGatewayAuditPort() {
        // durable Audit adapter가 없으면 auditReasonRequired route는 ProxyService가 fail-closed 처리합니다.
        return new CpfGatewayAuditPort() {
            @Override public boolean durable() { return false; }
            @Override public void record(com.cpf.core.api.gateway.CpfGatewayAuditEvent event) { }
        };
    }

    @Bean
    public RestClient cpfGatewayRestClient(RestClient.Builder builder) { return builder.build(); }
}
