package com.cpf.core.common.http;

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
public class CpfHttpClientProperties {

    /** Connection timeout in milliseconds. */
    private int connectTimeoutMillis = 3000;

    /** Response read timeout in milliseconds. */
    private int readTimeoutMillis = 5000;

    /** Maximum response body size buffered in memory, in KiB. */
    private int maxInMemorySizeKb = 2048;
}
