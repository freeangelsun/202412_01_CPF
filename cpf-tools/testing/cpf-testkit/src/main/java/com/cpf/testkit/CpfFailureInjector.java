package com.cpf.testkit;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public final class CpfFailureInjector {
    private final AtomicInteger remaining;
    private final Supplier<? extends RuntimeException> failure;
    public CpfFailureInjector(int failures, Supplier<? extends RuntimeException> failure) {
        if (failures < 0) throw new IllegalArgumentException("failures must be >= 0");
        this.remaining = new AtomicInteger(failures); this.failure = Objects.requireNonNull(failure);
    }
    public void check() { if (remaining.getAndUpdate(value -> Math.max(0, value - 1)) > 0) throw failure.get(); }
    public int remaining() { return remaining.get(); }
}
