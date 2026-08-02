package com.cpf.starter.messaging.reliability;

import com.cpf.core.common.broker.CpfBrokerEnvelope;
import com.cpf.core.common.broker.CpfBrokerOutboxPort;
import com.cpf.core.common.broker.CpfBrokerPublishResultProbe;
import com.cpf.core.common.broker.CpfBrokerResult;
import com.cpf.core.common.broker.CpfBrokerUnknownResultPort;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

/** Reconciles UNKNOWN broker results before any duplicate publication is allowed. */
public final class CpfBrokerUnknownResultReconciler {
    private final CpfBrokerUnknownResultPort unknownPort;
    private final CpfBrokerOutboxPort outboxPort;
    private final List<CpfBrokerPublishResultProbe> probes;
    private final Clock clock;
    private final Duration retryDelay;

    public CpfBrokerUnknownResultReconciler(
            CpfBrokerUnknownResultPort unknownPort,
            List<CpfBrokerPublishResultProbe> probes,
            Clock clock,
            Duration retryDelay) {
        this.unknownPort = Objects.requireNonNull(unknownPort, "unknownPort");
        if (!(unknownPort instanceof CpfBrokerOutboxPort outbox)) {
            throw new IllegalArgumentException(
                    "UNKNOWN result port must also implement CpfBrokerOutboxPort");
        }
        outboxPort = outbox;
        this.probes = probes == null ? List.of() : List.copyOf(probes);
        this.clock = Objects.requireNonNull(clock, "clock");
        if (retryDelay == null || retryDelay.isZero() || retryDelay.isNegative()) {
            throw new IllegalArgumentException("retryDelay must be positive");
        }
        this.retryDelay = retryDelay;
    }

    public Result runOnce(String workerId, int limit) {
        List<CpfBrokerEnvelope> claimed = unknownPort.claimUnknown(workerId, limit);
        int resolvedSuccess = 0;
        int resolvedFailure = 0;
        int pending = 0;
        for (CpfBrokerEnvelope envelope : claimed) {
            CpfBrokerResult result = probe(envelope);
            if (result != null && !isUnknown(result)) {
                // markPublished applies the durable retry/DLQ state machine for both success and failure.
                outboxPort.markPublished(envelope.message().messageId(), result);
                if (isPublished(result)) {
                    resolvedSuccess++;
                } else {
                    resolvedFailure++;
                }
                continue;
            }
            String detail = result == null
                    ? "No Provider reconciliation evidence"
                    : "Provider result remains UNKNOWN: " + safe(result.detail());
            unknownPort.releaseUnknown(
                    envelope.message().messageId(), detail, clock.instant().plus(retryDelay));
            pending++;
        }
        return new Result(claimed.size(), resolvedSuccess, resolvedFailure, pending);
    }

    private CpfBrokerResult probe(CpfBrokerEnvelope envelope) {
        CpfBrokerResult lastUnknown = null;
        for (CpfBrokerPublishResultProbe probe : probes) {
            try {
                CpfBrokerResult result = probe.probe(envelope);
                if (result != null) {
                    lastUnknown = result;
                    if (!isUnknown(result)) {
                        return result;
                    }
                }
            } catch (RuntimeException exception) {
                lastUnknown = new CpfBrokerResult(
                        "UNKNOWN", envelope.message().messageId(), "PROBE",
                        null, clock.instant(), safe(exception.getMessage()));
            }
        }
        return lastUnknown;
    }

    private static boolean isUnknown(CpfBrokerResult result) {
        return "UNKNOWN".equalsIgnoreCase(result.status())
                || "RESULT_UNKNOWN".equalsIgnoreCase(result.status());
    }

    private static boolean isPublished(CpfBrokerResult result) {
        return "PUBLISHED".equalsIgnoreCase(result.status())
                || "SUCCESS".equalsIgnoreCase(result.status())
                || "ACCEPTED".equalsIgnoreCase(result.status());
    }

    private static String safe(String value) {
        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }
        return value.substring(0, Math.min(value.length(), 1000));
    }

    public record Result(
            int claimed,
            int resolvedSuccess,
            int resolvedFailure,
            int pending) {
    }
}
