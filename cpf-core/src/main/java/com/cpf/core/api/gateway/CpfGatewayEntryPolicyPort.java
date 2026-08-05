package com.cpf.core.api.gateway;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Gateway 외부 진입점의 listener/TLS/protocol/maintenance 경계를 평가하는 Public Port입니다.
 *
 * <p>구현체는 요청 처리보다 먼저 호출되어야 하며, 동일 version의 다른 명령을 거부하고
 * Snapshot 교체를 원자적으로 수행해야 합니다. 원문 Credential이나 Payload를 이 계약에
 * 전달하지 않습니다.</p>
 */
public interface CpfGatewayEntryPolicyPort {

    /** Data Plane 진입 상태입니다. */
    enum State {
        ACTIVE,
        MAINTENANCE,
        DRAINING
    }

    /** Provider 독립적인 진입 요청 Metadata입니다. */
    record Request(
            String path,
            String method,
            String protocol,
            boolean secure,
            int localPort,
            String remoteAddress,
            Instant requestedAt) {
        public Request {
            path = path == null || path.isBlank() ? "/" : path.trim();
            method = method == null || method.isBlank()
                    ? "UNKNOWN" : method.trim().toUpperCase(Locale.ROOT);
            protocol = protocol == null || protocol.isBlank()
                    ? "UNKNOWN" : protocol.trim().toUpperCase(Locale.ROOT);
            if (localPort < 0 || localPort > 65_535) {
                throw new IllegalArgumentException("localPort out of range");
            }
            remoteAddress = remoteAddress == null ? "" : remoteAddress.trim();
            requestedAt = requestedAt == null ? Instant.now() : requestedAt;
        }
    }


    /** 민감한 식별자 없이 누적되는 Entry 운영 Telemetry입니다. */
    record Telemetry(
            long allowed,
            long denied,
            long portDenied,
            long protocolDenied,
            long tlsDenied,
            long maintenanceDenied,
            Instant observedAt) {
        public Telemetry {
            if (allowed < 0L || denied < 0L || portDenied < 0L || protocolDenied < 0L
                    || tlsDenied < 0L || maintenanceDenied < 0L) {
                throw new IllegalArgumentException("telemetry counters must not be negative");
            }
            if (portDenied + protocolDenied + tlsDenied + maintenanceDenied > denied) {
                throw new IllegalArgumentException("denial categories exceed total denied");
            }
            observedAt = observedAt == null ? Instant.now() : observedAt;
        }
    }

    /** 원자적으로 적용되는 운영 상태 Snapshot입니다. */
    record Snapshot(long version, State state, Duration retryAfter, Instant changedAt) {
        public Snapshot {
            if (version < 0L) throw new IllegalArgumentException("version must not be negative");
            state = Objects.requireNonNull(state, "state");
            retryAfter = retryAfter == null ? Duration.ZERO : retryAfter;
            if (retryAfter.isNegative()) throw new IllegalArgumentException("retryAfter must not be negative");
            changedAt = changedAt == null ? Instant.now() : changedAt;
        }
    }

    /** 요청별 진입 판정입니다. */
    record Decision(
            boolean allowed,
            int httpStatus,
            State state,
            Duration retryAfter,
            String reason,
            long policyVersion) {
        public Decision {
            if (httpStatus < 100 || httpStatus > 599) {
                throw new IllegalArgumentException("httpStatus out of range");
            }
            state = Objects.requireNonNull(state, "state");
            retryAfter = retryAfter == null ? Duration.ZERO : retryAfter;
            if (retryAfter.isNegative()) throw new IllegalArgumentException("retryAfter must not be negative");
            reason = reason == null || reason.isBlank() ? "UNSPECIFIED" : reason.trim();
            if (policyVersion < 0L) throw new IllegalArgumentException("policyVersion must not be negative");
        }

        public static Decision allow(Snapshot snapshot) {
            return new Decision(true, 200, snapshot.state(), Duration.ZERO, "ALLOWED", snapshot.version());
        }

        public static Decision deny(int status, Snapshot snapshot, String reason) {
            return new Decision(false, status, snapshot.state(), Duration.ZERO, reason, snapshot.version());
        }

        public static Decision denyWithRetry(int status, Snapshot snapshot, String reason) {
            return new Decision(false, status, snapshot.state(), snapshot.retryAfter(), reason, snapshot.version());
        }
    }

    Snapshot snapshot();

    /** 운영 조회·Metric Adapter가 소비하는 비식별 누적값입니다. */
    Telemetry telemetry();

    /** expectedVersion이 현재 version과 일치할 때만 다음 운영 상태를 적용합니다. */
    Snapshot replace(long expectedVersion, long nextVersion, State state, Duration retryAfter);

    Decision evaluate(Request request);
}
