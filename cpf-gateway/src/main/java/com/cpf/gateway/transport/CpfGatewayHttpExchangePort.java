package com.cpf.gateway.transport;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.net.URI;

/** Gateway Proxy의 HTTP 전송 SPI입니다. */
@FunctionalInterface
public interface CpfGatewayHttpExchangePort {
    CpfGatewayProxyResponse exchange(
            URI uri,
            HttpMethod method,
            HttpHeaders headers,
            CpfGatewayReplayableBody body,
            long timeoutMillis);
}
