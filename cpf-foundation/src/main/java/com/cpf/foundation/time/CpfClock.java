package com.cpf.foundation.time;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;

/** Deterministic CPF clock. Business code must inject this instead of calling now() directly. */
public final class CpfClock {
    private final Clock clock;
    public CpfClock(Clock clock) { this.clock = Objects.requireNonNull(clock, "clock"); }
    public static CpfClock system(ZoneId zone) { return new CpfClock(Clock.system(zone)); }
    public static CpfClock utc() { return new CpfClock(Clock.systemUTC()); }
    public static CpfClock fixed(Instant instant, ZoneId zone) { return new CpfClock(Clock.fixed(instant, zone)); }
    public Instant instant() { return clock.instant(); }
    public LocalDate today() { return LocalDate.now(clock); }
    public LocalDateTime localDateTime() { return LocalDateTime.now(clock); }
    public ZoneId zone() { return clock.getZone(); }
    public Clock unwrap() { return clock; }
    public CpfClock withZone(ZoneId zone) { return new CpfClock(clock.withZone(zone)); }
}
