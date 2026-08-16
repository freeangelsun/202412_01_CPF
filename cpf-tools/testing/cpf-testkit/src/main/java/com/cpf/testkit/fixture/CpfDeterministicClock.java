package com.cpf.testkit.fixture;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/** 테스트 중 시간을 명시적으로 제어하는 CPF 결정적 Clock. */
public final class CpfDeterministicClock extends Clock {
    private final AtomicReference<Instant> instant;
    private final ZoneId zone;

    public CpfDeterministicClock(Instant initial, ZoneId zone) {
        this.instant = new AtomicReference<>(Objects.requireNonNull(initial, "initial"));
        this.zone = Objects.requireNonNull(zone, "zone");
    }

    public static CpfDeterministicClock utc(Instant initial) {
        return new CpfDeterministicClock(initial, ZoneId.of("UTC"));
    }

    @Override public ZoneId getZone() { return zone; }
    @Override public Clock withZone(ZoneId newZone) { return new CpfDeterministicClock(instant(), newZone); }
    @Override public Instant instant() { return instant.get(); }

    public Instant advance(Duration duration) {
        Objects.requireNonNull(duration, "duration");
        return instant.updateAndGet(value -> value.plus(duration));
    }

    public void set(Instant value) { instant.set(Objects.requireNonNull(value, "value")); }
}
