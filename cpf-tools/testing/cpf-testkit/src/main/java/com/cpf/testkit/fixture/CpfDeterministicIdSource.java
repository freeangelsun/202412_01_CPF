package com.cpf.testkit.fixture;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/** transaction/execution/idempotency 테스트용 결정적 ID 공급기. */
public final class CpfDeterministicIdSource implements Supplier<String> {
    private final String prefix;
    private final AtomicLong sequence;

    public CpfDeterministicIdSource(String prefix, long firstSequence) {
        this.prefix = Objects.requireNonNull(prefix, "prefix");
        if (prefix.isBlank()) throw new IllegalArgumentException("prefix must not be blank");
        this.sequence = new AtomicLong(firstSequence);
    }

    @Override public String get() { return prefix + sequence.getAndIncrement(); }
    public long currentSequence() { return sequence.get(); }
}
