package com.cpf.integration.http.internal;

import io.netty.channel.ChannelOption;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import reactor.netty.http.client.HttpClient;

import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * 검증된 DNS address와 실제 Reactor Netty 연결 주소를 동일하게 고정합니다.
 * 요청 URI의 원래 hostname은 유지되므로 Host header와 TLS SNI/hostname verification은
 * 서비스 hostname을 사용하고, TCP 연결만 검증된 IP로 pin 됩니다.
 */
public final class CpfPinnedHttpConnectorFactory {
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;

    public CpfPinnedHttpConnectorFactory(CpfHttpClientProperties properties) {
        this(require(properties).getConnectTimeoutMillis(), properties.getReadTimeoutMillis());
    }

    CpfPinnedHttpConnectorFactory(int connectTimeoutMillis, int readTimeoutMillis) {
        if (connectTimeoutMillis < 1 || readTimeoutMillis < 1) {
            throw new IllegalArgumentException("HTTP timeout must be positive");
        }
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
    }

    static CpfPinnedHttpConnectorFactory secureDefault() {
        return new CpfPinnedHttpConnectorFactory(3000, 5000);
    }

    private static CpfHttpClientProperties require(CpfHttpClientProperties properties) {
        if (properties == null) throw new IllegalArgumentException("httpClientProperties is required");
        return properties;
    }

    public ReactorClientHttpConnector connector(CpfServiceEndpointRegistry.ResolvedEndpoint endpoint) {
        if (endpoint == null) throw new IllegalArgumentException("resolved endpoint is required");
        HttpClient client = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMillis)
                .responseTimeout(Duration.ofMillis(readTimeoutMillis))
                .remoteAddress(() -> new InetSocketAddress(endpoint.pinnedAddress(), endpoint.port()));
        return new ReactorClientHttpConnector(client);
    }
}
