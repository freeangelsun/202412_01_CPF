package com.cpf.reliability.runtime;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.reliability.api.CpfIdempotencyException;
import com.cpf.reliability.api.CpfIdempotencyFingerprintResolver;
import com.cpf.reliability.api.CpfIdempotencyResultCodec;
import com.cpf.reliability.api.CpfIdempotencyStore;
import com.cpf.reliability.api.CpfIdempotent;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;

/** Annotation Aspect와 Testkit이 공유하는 멱등 실행 Coordinator입니다. */
public final class CpfIdempotencyCoordinator {
    private final CpfIdempotencyProperties properties;
    private final CpfIdempotencyStore store;
    private final CpfIdempotencyFingerprintResolver fingerprintResolver;
    private final CpfIdempotencyResultCodec resultCodec;
    private final Clock clock;

    public CpfIdempotencyCoordinator(CpfIdempotencyProperties properties, CpfIdempotencyStore store,
            CpfIdempotencyFingerprintResolver fingerprintResolver, CpfIdempotencyResultCodec resultCodec, Clock clock) {
        this.properties = Objects.requireNonNull(properties);
        this.store = store;
        this.fingerprintResolver = Objects.requireNonNull(fingerprintResolver);
        this.resultCodec = Objects.requireNonNull(resultCodec);
        this.clock = Objects.requireNonNull(clock);
    }

    public Object execute(Method method, Object target, Object[] args, CpfIdempotent annotation, Callable<Object> action) throws Exception {
        if (!properties.isEnabled() || annotation == null) return action.call();
        CpfContext context = CpfContexts.requireCurrent();
        if (store == null) {
            if (properties.isFailClosed() || annotation.required()) throw new CpfIdempotencyException(
                    "CPF_IDEMPOTENCY_STORE_REQUIRED", "Durable CpfIdempotencyStore is required");
            return action.call();
        }
        String key = context.idempotencyKey();
        if (key == null || key.isBlank()) {
            if (annotation.required()) throw new CpfIdempotencyException("CPF_IDEMPOTENCY_KEY_REQUIRED", "Context idempotencyKey is required");
            return action.call();
        }
        String operation = annotation.operation().isBlank()
                ? method.getDeclaringClass().getName() + "." + method.getName() : annotation.operation();
        String fingerprint = fingerprintResolver.resolve(method, args, context);
        Instant now = clock.instant();
        var request = new CpfIdempotencyStore.AcquireRequest(key, operation, fingerprint,
                context.transactionId(), context.executionId(), now,
                now.plusSeconds(positive(annotation.ttlSeconds(), "ttlSeconds")),
                now.plusSeconds(positive(annotation.inProgressTimeoutSeconds(), "inProgressTimeoutSeconds")));
        CpfIdempotencyStore.AcquireResult acquired = Objects.requireNonNull(store.acquire(request), "acquire result");
        return switch (acquired.state()) {
            case ACQUIRED -> executeWinner(method, annotation, acquired.leaseToken(), action);
            case REPLAY -> replay(method, annotation, fingerprint, acquired);
            case IN_PROGRESS -> throw state("CPF_IDEMPOTENCY_IN_PROGRESS", "An execution with the same idempotency key is still in progress");
            case CONFLICT -> throw state("CPF_IDEMPOTENCY_CONFLICT", "Idempotency key was reused with a different payload");
            case UNKNOWN -> throw state("CPF_IDEMPOTENCY_UNKNOWN", "Previous execution outcome is UNKNOWN and requires reconcile");
        };
    }

    private Object executeWinner(Method method, CpfIdempotent annotation, String leaseToken, Callable<Object> action) throws Exception {
        try {
            Object result = action.call();
            CpfIdempotencyStore.StoredResult stored = annotation.replayResult()
                    ? resultCodec.encode(result, method.getReturnType())
                    : new CpfIdempotencyStore.StoredResult("none-v1", new byte[0], method.getReturnType().getName());
            store.complete(leaseToken, stored, clock.instant());
            return result;
        } catch (Exception e) {
            try { store.fail(leaseToken, new CpfIdempotencyStore.Failure(e.getClass().getName(), safeCode(e), true, Map.of()), clock.instant()); }
            catch (RuntimeException storeFailure) { e.addSuppressed(storeFailure); }
            throw e;
        } catch (Error e) {
            try { store.fail(leaseToken, new CpfIdempotencyStore.Failure(e.getClass().getName(), "JVM_ERROR", false, Map.of()), clock.instant()); }
            catch (RuntimeException storeFailure) { e.addSuppressed(storeFailure); }
            throw e;
        }
    }

    private Object replay(Method method, CpfIdempotent annotation, String fingerprint, CpfIdempotencyStore.AcquireResult acquired) {
        if (acquired.storedFingerprint() != null && !Objects.equals(fingerprint, acquired.storedFingerprint())) {
            throw state("CPF_IDEMPOTENCY_CONFLICT", "Stored payload fingerprint does not match current request");
        }
        if (!annotation.replayResult()) throw state("CPF_IDEMPOTENCY_DUPLICATE", "Duplicate completed invocation is not replayable");
        return resultCodec.decode(acquired.storedResult(), method.getReturnType());
    }

    private static long positive(long value, String name) {
        if (value < 1) throw new CpfIdempotencyException("CPF_IDEMPOTENCY_CONFIG_INVALID", name + " must be positive");
        return value;
    }
    private static CpfIdempotencyException state(String code, String message) { return new CpfIdempotencyException(code, message); }
    private static String safeCode(Throwable error) {
        if (error instanceof CpfIdempotencyException ie) return ie.code();
        return "BUSINESS_FAILURE";
    }
}
