package com.cpf.gateway.api;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Rate Limit Counter Provider SPI입니다.
 *
 * <p>다중 인스턴스 환경은 {@link #distributed()}가 {@code true}인 구현을 사용해야 하며,
 * client/channel/API/tenant 복합 Scope는 {@link #consumeAtomically(List)}에서 하나의 원자적
 * 판정으로 처리해야 합니다.</p>
 */
public interface CpfGatewayRateLimitCounterPort {
    CounterResult consume(CounterCommand command);

    /**
     * 여러 Scope의 Counter를 원자적으로 소비합니다.
     *
     * <p>기존 단일 Counter Provider는 한 건까지 호환되지만, 복합 Scope를 지원하려면 반드시
     * 이 메서드를 구현해야 합니다. 부분 소비를 허용하는 기본 반복 구현은 제공하지 않습니다.</p>
     */
    default BatchResult consumeAtomically(List<CounterCommand> commands) {
        Objects.requireNonNull(commands, "commands");
        if (commands.isEmpty()) {
            return new BatchResult(true, -1, List.of());
        }
        if (commands.size() == 1) {
            CounterResult result = consume(Objects.requireNonNull(commands.getFirst(), "command"));
            return new BatchResult(result.accepted(), result.accepted() ? -1 : 0, List.of(result));
        }
        throw new UnsupportedOperationException(
                "Provider must implement atomic multi-scope rate-limit consumption");
    }

    CounterHealth health();

    boolean distributed();

    record CounterCommand(
            long policyVersion,
            String counterKey,
            String requestId,
            long windowStartEpochMillis,
            long windowMillis,
            int quota,
            int burst,
            int units,
            int abuseThreshold,
            long blockMillis,
            long nowEpochMillis) {
        public CounterCommand {
            counterKey = required(counterKey, "counterKey");
            requestId = required(requestId, "requestId");
            if (policyVersion < 0L) {
                throw new IllegalArgumentException("policyVersion must not be negative");
            }
            if (windowStartEpochMillis < 0L || windowMillis < 1_000L || windowMillis > 86_400_000L) {
                throw new IllegalArgumentException("invalid rate-limit window");
            }
            if (nowEpochMillis < windowStartEpochMillis
                    || nowEpochMillis >= Math.addExact(windowStartEpochMillis, windowMillis)) {
                throw new IllegalArgumentException("nowEpochMillis must belong to the command window");
            }
            if (quota < 1 || burst < 0 || units < 1 || units > 1_000) {
                throw new IllegalArgumentException("invalid quota/burst/units");
            }
            if ((long) quota + burst > 20_000_000L) {
                throw new IllegalArgumentException("rate-limit capacity is too large");
            }
            if (abuseThreshold < 0 || blockMillis < 0L || blockMillis > 86_400_000L) {
                throw new IllegalArgumentException("invalid abuse policy");
            }
        }

        public long resetAtEpochMillis() {
            return Math.addExact(windowStartEpochMillis, windowMillis);
        }

        private static String required(String value, String name) {
            String normalized = value == null ? "" : value.trim();
            if (normalized.isEmpty()) throw new IllegalArgumentException(name + " is required");
            if (normalized.length() > 500) throw new IllegalArgumentException(name + " is too long");
            return normalized;
        }
    }

    record CounterResult(
            boolean accepted,
            boolean duplicate,
            long used,
            long remaining,
            long resetAtEpochMillis,
            long blockedUntilEpochMillis,
            int rejectedCount,
            String reason) {
        public CounterResult {
            if (used < 0L || remaining < 0L || rejectedCount < 0) {
                throw new IllegalArgumentException("counter values must not be negative");
            }
            reason = reason == null ? "" : reason.trim();
            if (reason.isEmpty() || reason.length() > 100) {
                throw new IllegalArgumentException("counter reason is required and must be bounded");
            }
            if (resetAtEpochMillis < 0L || blockedUntilEpochMillis < 0L) {
                throw new IllegalArgumentException("counter timestamps must not be negative");
            }
        }
    }

    record BatchResult(boolean accepted, int limitingIndex, List<CounterResult> results) {
        public BatchResult {
            results = results == null ? List.of() : List.copyOf(results);
            if (accepted && limitingIndex != -1) {
                throw new IllegalArgumentException("accepted batch must not have limitingIndex");
            }
            if (!accepted && (limitingIndex < 0 || limitingIndex >= results.size())) {
                throw new IllegalArgumentException("denied batch requires a valid limitingIndex");
            }
        }
    }

    record CounterHealth(boolean ready, long activeCounters, String status, Instant observedAt) {
        public CounterHealth {
            if (activeCounters < 0L) {
                throw new IllegalArgumentException("activeCounters must not be negative");
            }
            status = status == null ? "UNKNOWN" : status.trim();
            if (status.isEmpty() || status.length() > 100) {
                throw new IllegalArgumentException("counter health status is invalid");
            }
            observedAt = Objects.requireNonNull(observedAt, "observedAt");
        }
    }
}
