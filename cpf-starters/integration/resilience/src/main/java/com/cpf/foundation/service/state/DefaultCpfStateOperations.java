package com.cpf.foundation.service.state;

import com.cpf.platform.operations.api.state.CpfOperationState;
import com.cpf.platform.operations.api.state.CpfStateAuditEvent;
import com.cpf.platform.operations.api.state.CpfStateIdentifiers;
import com.cpf.platform.operations.api.state.CpfStateOperations;
import com.cpf.platform.operations.api.state.CpfStateQueryResult;
import com.cpf.platform.operations.api.state.CpfStateSearchRequest;
import com.cpf.platform.operations.api.state.CpfStateSearchResult;
import com.cpf.platform.operations.api.state.CpfStateSnapshot;
import com.cpf.platform.operations.api.state.CpfStateTransitionRequest;
import com.cpf.platform.operations.api.state.CpfStateTransitionResult;
import com.cpf.platform.operations.spi.state.CpfStateAuditSink;
import com.cpf.platform.operations.spi.state.CpfStateStore;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/** Optimistic, idempotent state transitions with typed UNKNOWN and bounded query semantics. */
public final class DefaultCpfStateOperations implements CpfStateOperations {
    private static final Map<CpfOperationState, Set<CpfOperationState>> ALLOWED = Map.of(
            CpfOperationState.NEW, EnumSet.of(CpfOperationState.RUNNING, CpfOperationState.CANCELLED),
            CpfOperationState.RUNNING, EnumSet.of(
                    CpfOperationState.UNKNOWN,
                    CpfOperationState.SUCCEEDED,
                    CpfOperationState.FAILED,
                    CpfOperationState.CANCELLED),
            CpfOperationState.UNKNOWN, EnumSet.of(
                    CpfOperationState.RUNNING,
                    CpfOperationState.SUCCEEDED,
                    CpfOperationState.FAILED,
                    CpfOperationState.CANCELLED),
            CpfOperationState.SUCCEEDED, EnumSet.noneOf(CpfOperationState.class),
            CpfOperationState.FAILED, EnumSet.noneOf(CpfOperationState.class),
            CpfOperationState.CANCELLED, EnumSet.noneOf(CpfOperationState.class));

    private final CpfStateStore store;
    private final Clock clock;
    private final List<CpfStateAuditSink> auditSinks;

    public DefaultCpfStateOperations(CpfStateStore store, Clock clock) {
        this(store, clock, List.of());
    }

    public DefaultCpfStateOperations(
            CpfStateStore store, CpfStateAuditSink auditSink, Clock clock) {
        this(store, clock, auditSink == null ? List.of() : List.of(auditSink));
    }

    public DefaultCpfStateOperations(
            CpfStateStore store, Clock clock, List<CpfStateAuditSink> auditSinks) {
        this.store = Objects.requireNonNull(store, "store");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.auditSinks = auditSinks == null
                ? List.of() : auditSinks.stream().filter(Objects::nonNull).toList();
    }

    @Override
    public CpfStateTransitionResult start(
            String stateKey, String operationId, String actor, String reason) {
        CpfStateTransitionRequest request = new CpfStateTransitionRequest(
                stateKey, -1L, CpfOperationState.RUNNING, operationId, actor, reason);
        CpfStateSnapshot attempted = snapshot(request, 0L);
        CpfStateStore.WriteResult write;
        try {
            write = store.compareAndSet(
                    request.stateKey(), -1L, request.operationId(), request.commandHash(), attempted);
        } catch (RuntimeException failure) {
            return result(CpfStateTransitionResult.Status.STORE_UNAVAILABLE, null,
                    "state store unavailable");
        }
        if (write.status() != CpfStateStore.Status.CONFLICT) {
            return audited(null, request, map(write, attempted));
        }
        CpfStateSnapshot current = write.snapshot();
        if (current == null) {
            return audited(null, request, result(
                    CpfStateTransitionResult.Status.UNKNOWN_RESULT, null,
                    "state creation conflict without current snapshot"));
        }
        if (current.state() == CpfOperationState.UNKNOWN) {
            return transition(new CpfStateTransitionRequest(
                    request.stateKey(), current.version(), CpfOperationState.RUNNING,
                    request.operationId(), request.actor(), request.reason()));
        }
        return audited(current, request, result(
                current.state().terminal()
                        ? CpfStateTransitionResult.Status.INVALID_TRANSITION
                        : CpfStateTransitionResult.Status.VERSION_CONFLICT,
                current,
                current.state().terminal()
                        ? "terminal state cannot be restarted" : "operation already running"));
    }

    @Override
    public CpfStateTransitionResult transition(CpfStateTransitionRequest request) {
        Objects.requireNonNull(request, "request");
        if (request.expectedVersion() < 0L) {
            return audited(null, request, result(
                    CpfStateTransitionResult.Status.INVALID_TRANSITION, null,
                    "transition requires a non-negative expectedVersion"));
        }

        CpfStateSnapshot current;
        try {
            Optional<CpfStateSnapshot> found = store.find(request.stateKey());
            if (found.isEmpty()) {
                return audited(null, request, result(
                        CpfStateTransitionResult.Status.NOT_FOUND, null, "state not found"));
            }
            current = found.get();
        } catch (RuntimeException failure) {
            return result(CpfStateTransitionResult.Status.STORE_UNAVAILABLE, null,
                    "state store unavailable");
        }

        if (current.version() != request.expectedVersion()) {
            CpfStateSnapshot attempted;
            try {
                attempted = snapshot(request, Math.addExact(request.expectedVersion(), 1L));
                return audited(current, request, map(store.compareAndSet(
                        request.stateKey(), request.expectedVersion(), request.operationId(),
                        request.commandHash(), attempted), attempted));
            } catch (ArithmeticException overflow) {
                return audited(current, request, result(
                        CpfStateTransitionResult.Status.RESOURCE_EXHAUSTED, current,
                        "state version exhausted"));
            } catch (RuntimeException failure) {
                return result(CpfStateTransitionResult.Status.STORE_UNAVAILABLE, null,
                        "state store unavailable");
            }
        }

        Set<CpfOperationState> allowed = ALLOWED.get(current.state());
        if (allowed == null || !allowed.contains(request.targetState())) {
            return audited(current, request, result(
                    CpfStateTransitionResult.Status.INVALID_TRANSITION, current,
                    "transition is not allowed"));
        }

        CpfStateSnapshot next;
        try {
            next = snapshot(request, Math.addExact(current.version(), 1L));
            return audited(current, request, map(store.compareAndSet(
                    current.stateKey(), current.version(), request.operationId(),
                    request.commandHash(), next), next));
        } catch (ArithmeticException overflow) {
            return audited(current, request, result(
                    CpfStateTransitionResult.Status.RESOURCE_EXHAUSTED, current,
                    "state version exhausted"));
        } catch (RuntimeException failure) {
            return result(CpfStateTransitionResult.Status.STORE_UNAVAILABLE, null,
                    "state store unavailable");
        }
    }

    @Override
    public CpfStateQueryResult query(String stateKey) {
        String key = CpfStateIdentifiers.stateKey(stateKey);
        try {
            Optional<CpfStateSnapshot> found = store.find(key);
            return found.map(value -> new CpfStateQueryResult(
                            CpfStateQueryResult.Status.FOUND, value))
                    .orElseGet(() -> new CpfStateQueryResult(
                            CpfStateQueryResult.Status.NOT_FOUND, null));
        } catch (RuntimeException failure) {
            return new CpfStateQueryResult(CpfStateQueryResult.Status.STORE_UNAVAILABLE, null);
        }
    }

    @Override
    public CpfStateSearchResult search(CpfStateSearchRequest request) {
        Objects.requireNonNull(request, "request");
        try {
            CpfStateStore.SearchResult found = store.search(request);
            return switch (found.status()) {
                case SUCCESS -> new CpfStateSearchResult(
                        CpfStateSearchResult.Status.SUCCESS, found.items(), found.nextCursor());
                case UNSUPPORTED -> new CpfStateSearchResult(
                        CpfStateSearchResult.Status.UNSUPPORTED, List.of(), null);
                case UNKNOWN -> new CpfStateSearchResult(
                        CpfStateSearchResult.Status.STORE_UNAVAILABLE, List.of(), null);
            };
        } catch (RuntimeException failure) {
            return new CpfStateSearchResult(
                    CpfStateSearchResult.Status.STORE_UNAVAILABLE, List.of(), null);
        }
    }

    private CpfStateSnapshot snapshot(CpfStateTransitionRequest request, long version) {
        return new CpfStateSnapshot(
                request.stateKey(), request.targetState(), version,
                request.operationId(), request.actor(), request.reason(), clock.instant());
    }

    private CpfStateTransitionResult audited(
            CpfStateSnapshot before,
            CpfStateTransitionRequest request,
            CpfStateTransitionResult decision) {
        if (auditSinks.isEmpty()) return decision;
        CpfStateSnapshot after = decision.snapshot();
        CpfStateAuditEvent event = new CpfStateAuditEvent(
                sha256(request.stateKey()),
                sha256(request.operationId()),
                request.actor(),
                before == null ? null : before.state(),
                request.targetState(),
                after == null ? null : after.state(),
                before == null ? -1L : before.version(),
                after == null ? -1L : after.version(),
                decision.status().name(),
                request.reason(),
                clock.instant());
        for (CpfStateAuditSink sink : auditSinks) {
            try {
                sink.record(event);
            } catch (RuntimeException auditFailure) {
                return decision.status() == CpfStateTransitionResult.Status.APPLIED
                        ? result(CpfStateTransitionResult.Status.UNKNOWN_RESULT,
                                decision.snapshot(),
                                "state applied but audit persistence is unknown")
                        : result(CpfStateTransitionResult.Status.AUDIT_UNAVAILABLE,
                                decision.snapshot(), "audit unavailable");
            }
        }
        return decision;
    }

    private static CpfStateTransitionResult map(
            CpfStateStore.WriteResult write, CpfStateSnapshot attempted) {
        if (write == null || write.status() == null) {
            return result(CpfStateTransitionResult.Status.UNKNOWN_RESULT, null,
                    "state provider returned an invalid result");
        }
        return switch (write.status()) {
            case APPLIED -> result(CpfStateTransitionResult.Status.APPLIED, attempted, "applied");
            case IDEMPOTENT_REPLAY -> result(
                    CpfStateTransitionResult.Status.IDEMPOTENT_REPLAY,
                    write.snapshot(), "idempotent replay");
            case CONFLICT -> result(
                    CpfStateTransitionResult.Status.VERSION_CONFLICT,
                    write.snapshot(), "compare-and-set conflict");
            case OPERATION_CONFLICT -> result(
                    CpfStateTransitionResult.Status.OPERATION_CONFLICT,
                    write.snapshot(), "operationId reused with a different command");
            case RESOURCE_EXHAUSTED -> result(
                    CpfStateTransitionResult.Status.RESOURCE_EXHAUSTED,
                    write.snapshot(), "state provider capacity exhausted");
            case UNKNOWN -> result(
                    CpfStateTransitionResult.Status.UNKNOWN_RESULT,
                    write.snapshot(), "state write result unknown");
        };
    }

    private static CpfStateTransitionResult result(
            CpfStateTransitionResult.Status status,
            CpfStateSnapshot snapshot,
            String message) {
        return new CpfStateTransitionResult(status, snapshot, message);
    }

    private static String sha256(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 unavailable", impossible);
        }
    }
}
