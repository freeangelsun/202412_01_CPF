package com.cpf.gateway.api;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Gateway의 client/channel/API/tenant Rate Limit 판정 계약입니다.
 *
 * <p>Provider 구현 유형을 노출하지 않으며, Data Plane Consumer는 이 계약의 판정과
 * {@code retryAfter}만 사용합니다.</p>
 */
public interface CpfGatewayRateLimitPort {
    Decision acquire(Request request);

    Health health();

    default PolicyStatus status() {
        Health health = health();
        return new PolicyStatus(0L, 0, 0, 0, 0, true, health);
    }

    enum Scope {
        API,
        CLIENT,
        CHANNEL,
        TENANT
    }

    record Request(
            String executionId,
            String routeId,
            String clientId,
            String channelId,
            String tenantId,
            String requestId,
            int permits,
            Instant requestedAt) {
        public Request {
            executionId = required(executionId, "executionId");
            routeId = required(routeId, "routeId");
            clientId = optional(clientId, "clientId");
            channelId = optional(channelId, "channelId");
            tenantId = optional(tenantId, "tenantId");
            requestId = required(requestId, "requestId");
            if (permits < 1 || permits > 1_000) {
                throw new IllegalArgumentException("permits must be between 1 and 1000");
            }
            requestedAt = Objects.requireNonNull(requestedAt, "requestedAt");
        }

        private static String required(String value, String name) {
            String normalized = text(value);
            if (normalized.isEmpty()) throw new IllegalArgumentException(name + " is required");
            if (normalized.length() > 200) throw new IllegalArgumentException(name + " is too long");
            return normalized;
        }

        private static String optional(String value, String name) {
            String normalized = text(value);
            if (normalized.length() > 200) {
                throw new IllegalArgumentException(name + " is too long");
            }
            return normalized;
        }

        private static String text(String value) {
            return value == null ? "" : value.trim();
        }
    }

    record Decision(
            boolean allowed,
            String policyId,
            Scope limitingScope,
            long remaining,
            Instant resetAt,
            Duration retryAfter,
            boolean duplicate,
            boolean degraded,
            String reason) {
        public Decision {
            policyId = policyId == null ? "" : policyId.trim();
            remaining = Math.max(0L, remaining);
            resetAt = Objects.requireNonNull(resetAt, "resetAt");
            retryAfter = Objects.requireNonNullElse(retryAfter, Duration.ZERO);
            if (retryAfter.isNegative()) throw new IllegalArgumentException("retryAfter must not be negative");
            reason = reason == null ? "" : reason.trim();
        }
    }

    record PolicyStatus(
            long version,
            int apiPolicies,
            int clientPolicies,
            int channelPolicies,
            int tenantPolicies,
            boolean failClosedOnCounterFailure,
            Health counterHealth) {
        public PolicyStatus {
            if (version < 0L || apiPolicies < 0 || clientPolicies < 0
                    || channelPolicies < 0 || tenantPolicies < 0) {
                throw new IllegalArgumentException("invalid rate-limit policy status");
            }
            counterHealth = Objects.requireNonNull(counterHealth, "counterHealth");
        }
    }

    record Health(
            boolean ready,
            boolean distributed,
            long activeCounters,
            String status,
            Instant observedAt) {
        public Health {
            activeCounters = Math.max(0L, activeCounters);
            status = status == null ? "UNKNOWN" : status.trim();
            observedAt = Objects.requireNonNull(observedAt, "observedAt");
        }
    }
}
