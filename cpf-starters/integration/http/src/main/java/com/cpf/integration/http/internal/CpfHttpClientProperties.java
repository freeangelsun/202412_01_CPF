package com.cpf.integration.http.internal;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * HTTP client defaults shared by CPF inter-service WebClient calls.
 *
 * <p>Service-specific base URLs are configured by {@link CpfServiceEndpointProperties}.</p>
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "cpf.http-client")
/** CpfHttpClientProperties 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public class CpfHttpClientProperties {

    /** Connection timeout in milliseconds. */
    private int connectTimeoutMillis = 3000;

    /** Response read timeout in milliseconds. */
    private int readTimeoutMillis = 5000;

    /** Maximum response body size buffered in memory, in KiB. */
    /** 설정값의 의미와 기본 동작을 명확히 하여 운영 설정 영향을 추적한다. */
    private int maxInMemorySizeKb = 2048;
}
