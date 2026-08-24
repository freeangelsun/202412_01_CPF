package com.cpf.integration.http.internal;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Service endpoint configuration for inter-service HTTP calls.
 *
 * <p>Example: {@code cpf.services.payment.base-url=http://localhost:8181}</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "cpf")
/** CpfServiceEndpointProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfServiceEndpointProperties {

    /** Generator/Service Registry에 등록된 임의의 serviceId를 키로 사용하는 endpoint 설정입니다. */
    private Map<String, ServiceEndpoint> services = new LinkedHashMap<>();

    @Getter
    @Setter
    public static class ServiceEndpoint {

        /** Base URL of the target service. In production this should point to VIP, DNS, or Kubernetes Service. */
        /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
        private String baseUrl;

        /** Human-readable service description for documentation and diagnostics. */
        private String description;

        /** Hostname 사용을 명시적으로 허용합니다. 연결 직전 DNS 검증과 address pinning이 함께 적용됩니다. */
        private boolean allowDns;

        /** RFC1918/ULA 주소를 명시적으로 허용합니다. */
        private boolean allowPrivate;

        /** Public routable 주소 허용 여부입니다. */
        private boolean allowPublic = true;

        /** TLS 필수 여부입니다. 기본값은 true이며 false는 명시적으로 허용된 Literal IP 개발망에만 사용합니다. */
        private boolean requireTls = true;

        /** 허용 CIDR. 비어 있으면 address class 정책만 적용합니다. */
        private List<String> allowedCidrs = List.of();

        /** 허용 Port. 비어 있으면 TLS 기본 Port 정책을 사용합니다. */
        private List<Integer> allowedPorts = List.of(443, 8443, 9443);

        /** DNS 결과가 반드시 일치해야 하는 운영 Pin 목록입니다. */
        private List<String> pinnedAddresses = List.of();
    }
}
