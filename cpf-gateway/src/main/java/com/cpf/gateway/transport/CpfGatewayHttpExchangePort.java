package com.cpf.gateway.transport;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.net.URI;

/** Gateway Proxy의 HTTP 전송 SPI입니다. Route별 연결·응답·전체 Timeout 계약을 보존합니다. */
@FunctionalInterface
public interface CpfGatewayHttpExchangePort {
    CpfGatewayProxyResponse exchange(
            URI uri,
            HttpMethod method,
            HttpHeaders headers,
            CpfGatewayReplayableBody body,
            TimeoutPolicy timeoutPolicy);

    /**
     * connect timeout은 TCP/TLS 연결 단계, response timeout은 응답 header 수신 단계에 적용합니다.
     * overall timeout은 Retry/Failover를 포함한 Service Call Engine 전체 시간 상한이며 전송 Adapter는
     * 단일 시도가 이를 초과하지 않도록 response timeout과 함께 최소값을 사용합니다.
     */
    record TimeoutPolicy(
            long connectTimeoutMillis,
            long responseTimeoutMillis,
            long overallTimeoutMillis) {
        public TimeoutPolicy {
            if (connectTimeoutMillis <= 0L) {
                throw new IllegalArgumentException("connectTimeoutMillis must be positive");
            }
            if (responseTimeoutMillis <= 0L) {
                throw new IllegalArgumentException("responseTimeoutMillis must be positive");
            }
            if (overallTimeoutMillis <= 0L) {
                throw new IllegalArgumentException("overallTimeoutMillis must be positive");
            }
            if (connectTimeoutMillis > overallTimeoutMillis) {
                throw new IllegalArgumentException("connectTimeoutMillis cannot exceed overallTimeoutMillis");
            }
        }

        public long effectiveResponseTimeoutMillis() {
            return Math.min(responseTimeoutMillis, overallTimeoutMillis);
        }
    }
}
