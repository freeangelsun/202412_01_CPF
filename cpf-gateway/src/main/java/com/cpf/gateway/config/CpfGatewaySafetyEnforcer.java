package com.cpf.gateway.config;

import com.cpf.core.api.gateway.CpfGatewayRoute;
import com.cpf.core.api.logging.policy.LogPolicyDecision;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

/** 설치 안전 상한을 Route Apply·요청·응답·Capture 정책에 동일하게 강제합니다. */
@Component
public final class CpfGatewaySafetyEnforcer {
    private final CpfGatewaySafetyProperties properties;

    public CpfGatewaySafetyEnforcer(CpfGatewaySafetyProperties properties) {
        this.properties = properties;
        properties.validate();
    }

    public void validateRoute(CpfGatewayRoute route) {
        if (route == null) throw new IllegalArgumentException("Gateway route is required");
        ceiling(route.connectTimeoutMs(), properties.getConnectTimeoutCap(), "connectTimeoutMs");
        ceiling(route.responseTimeoutMs(), properties.getResponseTimeoutCap(), "responseTimeoutMs");
        ceiling(route.overallTimeoutMs(), properties.getOverallTimeoutCap(), "overallTimeoutMs");
        if (route.maxRetryCount() > properties.getRetryCountCap()) {
            throw new IllegalArgumentException("Gateway route retry 상한 초과: " + route.maxRetryCount());
        }
        if (route.maxRetryCount() > 0 && !route.idempotent()) {
            throw new IllegalArgumentException("비멱등 Gateway route에는 retry를 허용하지 않습니다.");
        }
    }

    public void validateRequest(HttpHeaders headers, long bodyBytes) {
        validateHeaders(headers);
        if (bodyBytes < -1L) {
            throw new IllegalArgumentException("Gateway request body 길이가 올바르지 않습니다.");
        }
        if (bodyBytes >= 0L && bodyBytes > properties.getRequestBodyBytesCap()) {
            throw new IllegalArgumentException("Gateway request body 안전 상한을 초과했습니다.");
        }
    }

    public void validateResponse(HttpHeaders headers) {
        validateHeaders(headers);
        long contentLength = headers == null ? -1 : headers.getContentLength();
        if (contentLength > properties.getResponseBodyBytesCap()) {
            throw new IllegalStateException("Gateway response body 안전 상한을 초과했습니다.");
        }
    }

    public void validateLogPolicy(LogPolicyDecision policy) {
        if (policy.maxHeaderBytes() > properties.getHeaderBytesCap())
            throw new IllegalArgumentException("Log Policy header capture 상한이 설치 상한을 초과했습니다.");
        if (policy.maxRequestBodyBytes() > properties.getRequestBodyBytesCap())
            throw new IllegalArgumentException("Log Policy request capture 상한이 설치 상한을 초과했습니다.");
        if (policy.maxResponseBodyBytes() > properties.getResponseBodyBytesCap())
            throw new IllegalArgumentException("Log Policy response capture 상한이 설치 상한을 초과했습니다.");
    }

    public long requestBodyBytesCap() { return properties.getRequestBodyBytesCap(); }
    public long responseBodyBytesCap() { return properties.getResponseBodyBytesCap(); }

    private void validateHeaders(HttpHeaders headers) {
        if (headers == null) return;
        if (headers.size() > properties.getHeaderCountCap()) {
            throw new IllegalArgumentException("Gateway header 개수 안전 상한을 초과했습니다.");
        }
        long bytes = 0;
        for (var entry : headers.entrySet()) {
            bytes += entry.getKey().getBytes(StandardCharsets.UTF_8).length;
            for (String value : entry.getValue()) {
                bytes += value == null ? 0 : value.getBytes(StandardCharsets.UTF_8).length;
                if (bytes > properties.getHeaderBytesCap()) {
                    throw new IllegalArgumentException("Gateway header 크기 안전 상한을 초과했습니다.");
                }
            }
        }
    }

    private static void ceiling(int actualMillis, Duration cap, String field) {
        if (actualMillis > cap.toMillis()) {
            throw new IllegalArgumentException("Gateway route " + field + " 설치 상한 초과: " + actualMillis);
        }
    }
}
