package com.cpf.gateway.config;

import com.cpf.core.api.gateway.CpfGatewayAuditPort;
import com.cpf.core.api.gateway.CpfGatewayAuthenticationPort;
import com.cpf.core.api.gateway.CpfGatewayAuthorizationPort;
import com.cpf.core.api.runtimecontrol.CpfRuntimeChangeApplier;
import com.cpf.gateway.route.CpfGatewayRouteSnapshot;
import com.cpf.gateway.runtime.CpfApiClientSecurityPolicy;
import com.cpf.gateway.runtime.CpfApiClientSecurityRuntimeApplier;
import com.cpf.gateway.runtime.CpfGatewayRouteRuntimeApplier;
import com.cpf.gateway.runtime.CpfGatewayRuntimeApplier;
import com.cpf.gateway.runtime.CpfGatewayRuntimePolicy;
import com.cpf.gateway.transport.CpfGatewayHttpExchangePort;
import com.cpf.gateway.transport.CpfGatewayTransferPolicy;
import com.cpf.gateway.transport.JdkCpfGatewayHttpExchangeAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.nio.file.Path;

/** CPF Gateway runtime의 data plane Bean을 구성합니다. */
@Configuration
@EnableScheduling
public class CpfGatewayConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CpfApiClientSecurityPolicy cpfApiClientSecurityPolicy() {
        return new CpfApiClientSecurityPolicy();
    }

    @Bean(name = "cpfApiClientSecurityRuntimeApplier")
    @ConditionalOnMissingBean(name = "cpfApiClientSecurityRuntimeApplier")
    public CpfRuntimeChangeApplier cpfApiClientSecurityRuntimeApplier(CpfApiClientSecurityPolicy policy) {
        return new CpfApiClientSecurityRuntimeApplier(policy);
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfGatewayRuntimePolicy cpfGatewayRuntimePolicy() {
        return new CpfGatewayRuntimePolicy();
    }

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
        // 고객사 OAuth2/JWT/mTLS Adapter는 동일 Port Bean으로 교체할 수 있습니다.
        return (route, credentials) -> policy.authenticate(
                credentials.get(com.cpf.core.api.header.CpfHeaderNames.API_KEY),
                credentials.get("cpf.client.ip"),
                credentials.get("cpf.client.cert.serial"));
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfGatewayAuthorizationPort cpfGatewayAuthorizationPort() {
        // 실제 Principal authority와 route permission을 비교하며, 보호 route는 권한 미확인 시 fail-closed 합니다.
        return (route, trustedHeaders) -> {
            String required = route.requiredPermission();
            if (required == null || required.isBlank()) return true;
            String authorities = trustedHeaders.get("cpf.principal.authorities");
            if (authorities == null || authorities.isBlank()) return false;
            for (String authority : authorities.split(",")) {
                if (required.equals(authority.trim())) return true;
            }
            return false;
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfGatewayAuditPort cpfGatewayAuditPort() {
        // durable Audit adapter가 없으면 auditReasonRequired route는 ProxyService가 fail-closed 처리합니다.
        return new CpfGatewayAuditPort() {
            @Override
            public boolean durable() {
                return false;
            }

            @Override
            public void record(com.cpf.core.api.gateway.CpfGatewayAuditEvent event) {
                // no-op default; 위험 거래는 durable=false로 차단됩니다.
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfGatewayTransferPolicy cpfGatewayTransferPolicy(
            CpfGatewaySafetyProperties safety,
            @Value("${cpf.gateway.transfer.max-request-bytes:1073741824}") long maxRequestBytes,
            @Value("${cpf.gateway.transfer.memory-threshold-bytes:1048576}") int memoryThresholdBytes,
            @Value("${cpf.gateway.transfer.io-buffer-bytes:65536}") int ioBufferBytes,
            @Value("${cpf.gateway.transfer.connect-timeout-millis:3000}") long connectTimeoutMillis,
            @Value("${cpf.gateway.transfer.request-timeout-millis:30000}") long requestTimeoutMillis,
            @Value("${cpf.gateway.transfer.temp-directory:${java.io.tmpdir}/cpf-gateway}") String tempDirectory) {
        safety.validate();
        long effectiveRequestBytes = Math.min(maxRequestBytes, safety.getRequestBodyBytesCap());
        int effectiveMemoryThreshold = (int) Math.min(memoryThresholdBytes, effectiveRequestBytes);
        long effectiveConnectTimeout = Math.min(connectTimeoutMillis, safety.getConnectTimeoutCap().toMillis());
        long effectiveRequestTimeout = Math.min(requestTimeoutMillis, safety.getOverallTimeoutCap().toMillis());
        return new CpfGatewayTransferPolicy(
                effectiveRequestBytes,
                effectiveMemoryThreshold,
                ioBufferBytes,
                effectiveConnectTimeout,
                Math.max(effectiveConnectTimeout, effectiveRequestTimeout),
                Path.of(tempDirectory));
    }

    @Bean
    @ConditionalOnMissingBean
    public CpfGatewayHttpExchangePort cpfGatewayHttpExchangePort(CpfGatewayTransferPolicy policy) {
        return new JdkCpfGatewayHttpExchangeAdapter();
    }
}
