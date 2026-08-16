package com.cpf.gateway.config;

import com.cpf.gateway.api.CpfGatewayRateLimitCounterPort;
import com.cpf.gateway.control.CpfGatewayControlSecurityProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** 기동 시 설치 안전 상한의 모순을 fail-fast 합니다. */
@Component
final class CpfGatewaySafetyStartupValidator {
    private final CpfGatewaySafetyProperties properties;
    private final CpfGatewayRateLimitCounterPort counters;
    private final Environment environment;
    private final CpfGatewayControlSecurityProperties control;
    CpfGatewaySafetyStartupValidator(
            CpfGatewaySafetyProperties properties,
            CpfGatewayRateLimitCounterPort counters,
            Environment environment,
            CpfGatewayControlSecurityProperties control) {
        this.properties = properties;
        this.counters = counters;
        this.environment = environment;
        this.control = control;
    }
    @PostConstruct void validate() {
        properties.validate();
        if (properties.isRequireDistributedRateLimitCounter() && !counters.distributed()) {
            throw new IllegalStateException("Distributed Gateway rate-limit counter is required");
        }
        if (!counters.health().ready()) {
            throw new IllegalStateException("Gateway rate-limit counter is not ready");
        }
        int serverPort = environment.getProperty("server.port", Integer.class, 8080);
        if (properties.getDataPlanePort() > 0 && properties.getDataPlanePort() != serverPort) {
            throw new IllegalStateException("cpf.gateway.data-plane-port must match server.port");
        }
        boolean sslEnabled = environment.getProperty("server.ssl.enabled", Boolean.class, false);
        if (properties.isRequireTlsIngress() && !sslEnabled) {
            throw new IllegalStateException("TLS ingress is required but server.ssl.enabled=false");
        }
        if (control.isEnabled()) {
            control.validate();
            if (control.getListenerPort() == serverPort) {
                throw new IllegalStateException("Gateway Control/Data Plane listener ports must differ");
            }
            String code = properties.getEnvironmentCode().trim().toLowerCase(java.util.Locale.ROOT);
            if (("prod".equals(code) || "production".equals(code)) && !control.isTlsEnabled()) {
                throw new IllegalStateException("Production Gateway Control Plane requires TLS");
            }
        }
    }
}
