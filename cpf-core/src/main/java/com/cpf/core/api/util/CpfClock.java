package com.cpf.core.api.util;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * 테스트 가능한 시간 의존성을 만들기 위한 CPF Clock wrapper입니다.
 * 업무 Source에서 LocalDate.now()/Instant.now()를 직접 흩뿌리지 않도록 합니다.
 */
public final class CpfClock {
    private final Clock clock;
    public CpfClock(Clock clock) { this.clock = clock == null ? Clock.systemDefaultZone() : clock; }
    public static CpfClock system() { return new CpfClock(Clock.systemDefaultZone()); }
    public static CpfClock utc() { return new CpfClock(Clock.systemUTC()); }
    public static CpfClock fixed(Instant instant, ZoneId zoneId) { return new CpfClock(Clock.fixed(instant, zoneId)); }
    public Instant instant() { return clock.instant(); }
    public LocalDate today() { return LocalDate.now(clock); }
    public ZoneId zone() { return clock.getZone(); }
    public Clock unwrap() { return clock; }
}
