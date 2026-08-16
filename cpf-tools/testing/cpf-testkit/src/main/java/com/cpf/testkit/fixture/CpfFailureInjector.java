package com.cpf.testkit.fixture;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/** retry/UNKNOWN/recovery 검증을 위한 thread-safe one-shot failure injector. */
public final class CpfFailureInjector {
    public enum Mode { SUCCESS, FAIL, UNKNOWN }
    private final ConcurrentHashMap<String, AtomicReference<Mode>> modes = new ConcurrentHashMap<>();

    public void failNext(String operation) { arm(operation, Mode.FAIL); }
    public void unknownNext(String operation) { arm(operation, Mode.UNKNOWN); }
    public void clear(String operation) { modes.remove(normalize(operation)); }

    public Mode consume(String operation) {
        AtomicReference<Mode> ref = modes.get(normalize(operation));
        if (ref == null) return Mode.SUCCESS;
        Mode result = ref.getAndSet(Mode.SUCCESS);
        return result == null ? Mode.SUCCESS : result;
    }

    private void arm(String operation, Mode mode) {
        modes.computeIfAbsent(normalize(operation), ignored -> new AtomicReference<>(Mode.SUCCESS)).set(mode);
    }

    private static String normalize(String operation) {
        String value = Objects.requireNonNull(operation, "operation").trim();
        if (value.isEmpty()) throw new IllegalArgumentException("operation must not be blank");
        return value;
    }
}
