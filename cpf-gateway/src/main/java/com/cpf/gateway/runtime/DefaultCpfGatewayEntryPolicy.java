package com.cpf.gateway.runtime;

import com.cpf.gateway.api.CpfGatewayEntryPolicyPort;
import com.cpf.gateway.config.CpfGatewaySafetyProperties;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

/** Gateway 설치 상한과 운영 상태를 결합하는 기본 Entry Policy입니다. */
public final class DefaultCpfGatewayEntryPolicy implements CpfGatewayEntryPolicyPort {
    private final CpfGatewaySafetyProperties safety;
    private final Clock clock;
    private final AtomicReference<Snapshot> snapshot;
    private final LongAdder allowed = new LongAdder();
    private final LongAdder denied = new LongAdder();
    private final LongAdder portDenied = new LongAdder();
    private final LongAdder protocolDenied = new LongAdder();
    private final LongAdder tlsDenied = new LongAdder();
    private final LongAdder maintenanceDenied = new LongAdder();

    public DefaultCpfGatewayEntryPolicy(CpfGatewaySafetyProperties safety) {
        this(safety, Clock.systemUTC());
    }

    DefaultCpfGatewayEntryPolicy(CpfGatewaySafetyProperties safety, Clock clock) {
        this.safety = Objects.requireNonNull(safety, "safety");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.snapshot = new AtomicReference<>(new Snapshot(
                0L,
                safety.isMaintenanceMode() ? State.MAINTENANCE : State.ACTIVE,
                safety.getMaintenanceRetryAfter(),
                Instant.now(clock)));
    }

    @Override
    public Snapshot snapshot() {
        return snapshot.get();
    }

    @Override
    public Telemetry telemetry() {
        return new Telemetry(
                allowed.sum(),
                denied.sum(),
                portDenied.sum(),
                protocolDenied.sum(),
                tlsDenied.sum(),
                maintenanceDenied.sum(),
                Instant.now(clock));
    }

    @Override
    public Snapshot replace(
            long expectedVersion,
            long nextVersion,
            State state,
            Duration retryAfter) {
        Snapshot next = new Snapshot(nextVersion, state, boundedRetry(retryAfter), Instant.now(clock));
        for (;;) {
            Snapshot current = snapshot.get();
            if (next.version() < current.version()) {
                throw new IllegalStateException("GATEWAY_ENTRY stale version");
            }
            if (next.version() == current.version()) {
                if (samePolicy(current, next)) return current;
                throw new IllegalStateException("GATEWAY_ENTRY same-version payload conflict");
            }
            if (current.version() != expectedVersion) {
                throw new IllegalStateException("GATEWAY_ENTRY expectedVersion conflict");
            }
            if (snapshot.compareAndSet(current, next)) return next;
        }
    }

    @Override
    public Decision evaluate(Request request) {
        Objects.requireNonNull(request, "request");
        Snapshot current = snapshot.get();
        int configuredPort = safety.getDataPlanePort();
        if (configuredPort > 0 && request.localPort() != configuredPort) {
            return deny(Decision.deny(404, current, "DATA_PLANE_PORT_MISMATCH"), portDenied);
        }
        if (!allowedProtocol(request.protocol(), safety.getAllowedIngressProtocols())) {
            return deny(Decision.deny(505, current, "INGRESS_PROTOCOL_DENIED"), protocolDenied);
        }
        if (safety.isRequireTlsIngress() && !request.secure()) {
            return deny(Decision.deny(426, current, "INGRESS_TLS_REQUIRED"), tlsDenied);
        }
        if (current.state() == State.MAINTENANCE) {
            return deny(Decision.denyWithRetry(503, current, "GATEWAY_MAINTENANCE"), maintenanceDenied);
        }
        if (current.state() == State.DRAINING) {
            return deny(Decision.denyWithRetry(503, current, "GATEWAY_DRAINING"), maintenanceDenied);
        }
        allowed.increment();
        return Decision.allow(current);
    }

    private Decision deny(Decision decision, LongAdder category) {
        denied.increment();
        category.increment();
        return decision;
    }

    private Duration boundedRetry(Duration retryAfter) {
        Duration value = retryAfter == null ? safety.getMaintenanceRetryAfter() : retryAfter;
        if (value.isNegative() || value.compareTo(safety.getMaintenanceRetryAfterCap()) > 0) {
            throw new IllegalArgumentException("retryAfter exceeds installation cap");
        }
        return value;
    }

    private static boolean samePolicy(Snapshot left, Snapshot right) {
        return left.state() == right.state() && left.retryAfter().equals(right.retryAfter());
    }

    private static boolean allowedProtocol(String actual, Set<String> allowed) {
        String normalized = actual == null ? "" : actual.trim().toUpperCase(Locale.ROOT);
        if ("HTTP/2".equals(normalized)) normalized = "HTTP/2.0";
        return allowed.contains(normalized);
    }
}
