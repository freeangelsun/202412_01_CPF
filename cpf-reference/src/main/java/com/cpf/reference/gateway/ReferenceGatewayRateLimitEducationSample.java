package com.cpf.reference.gateway;

import com.cpf.core.api.gateway.CpfGatewayRateLimitPort;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Gateway Rate Limit Public API를 업무 Consumer에서 사용하는 교육용 예제입니다.
 *
 * <p>Client가 전달한 거래 ID를 중복 판정 키로 신뢰하지 않고, Consumer가 생성한
 * operation ID를 사용합니다. 거부 결과는 무조건 재시도하지 않고 {@code retryAfter}와
 * {@code resetAt}을 호출자에게 전달해야 합니다.</p>
 */
public final class ReferenceGatewayRateLimitEducationSample {
    private final CpfGatewayRateLimitPort rateLimitPort;
    private final Clock clock;

    public ReferenceGatewayRateLimitEducationSample(CpfGatewayRateLimitPort rateLimitPort) {
        this(rateLimitPort, Clock.systemUTC());
    }

    ReferenceGatewayRateLimitEducationSample(CpfGatewayRateLimitPort rateLimitPort, Clock clock) {
        this.rateLimitPort = Objects.requireNonNull(rateLimitPort, "rateLimitPort");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public GatewayDecision check(
            String executionId,
            String routeId,
            String clientId,
            String channelId,
            String tenantId) {
        String operationId = UUID.randomUUID().toString();
        CpfGatewayRateLimitPort.Decision decision = rateLimitPort.acquire(
                new CpfGatewayRateLimitPort.Request(
                        required(executionId, "executionId"),
                        required(routeId, "routeId"),
                        text(clientId),
                        text(channelId),
                        text(tenantId),
                        operationId,
                        1,
                        Instant.now(clock)));
        return new GatewayDecision(
                decision.allowed(),
                decision.remaining(),
                decision.resetAt(),
                decision.retryAfter().toSeconds(),
                decision.degraded(),
                decision.reason());
    }

    private static String required(String value, String name) {
        String normalized = text(value);
        if (normalized.isEmpty()) throw new IllegalArgumentException(name + " is required");
        return normalized;
    }

    private static String text(String value) {
        return value == null ? "" : value.trim();
    }

    public record GatewayDecision(
            boolean allowed,
            long remaining,
            Instant resetAt,
            long retryAfterSeconds,
            boolean degraded,
            String reason) {
    }
}
