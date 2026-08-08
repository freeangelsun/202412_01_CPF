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
    /** Clock wrapper를 생성합니다. null Clock은 시스템 기본 Zone Clock으로 대체합니다.
     * @param clock 사용할 Clock. null이면 systemDefaultZone Clock
     */
    public CpfClock(Clock clock) { this.clock = clock == null ? Clock.systemDefaultZone() : clock; }
    /** 시스템 기본 Zone Clock wrapper를 생성합니다.
     * @return 검증된 CPF 값 객체
     */
    public static CpfClock system() { return new CpfClock(Clock.systemDefaultZone()); }
    /** UTC Clock wrapper를 생성합니다.
     * @return 검증된 CPF 값 객체
     */
    public static CpfClock utc() { return new CpfClock(Clock.systemUTC()); }
    /** 테스트용 고정 Clock wrapper를 생성합니다.
     * @param instant 고정 Clock 기준 Instant
     * @param zoneId null이 아닌 시간대
     * @return 검증된 CPF 값 객체
     * @throws NullPointerException instant 또는 zoneId가 null인 경우
     */
    public static CpfClock fixed(Instant instant, ZoneId zoneId) { return new CpfClock(Clock.fixed(instant, zoneId)); }
    /** 주입된 Clock의 현재 Instant를 반환합니다.
     * @return 변환된 Instant 또는 계약상 null
     */
    public Instant instant() { return clock.instant(); }
    /** 주입된 Clock 기준 LocalDate를 반환합니다.
     * @return 변환된 날짜 또는 계약상 null
     */
    public LocalDate today() { return LocalDate.now(clock); }
    /** 주입된 Clock의 Zone을 반환합니다.
     * @return Clock의 ZoneId
     */
    public ZoneId zone() { return clock.getZone(); }
    /** 저수준 연계가 필요할 때 원본 java.time.Clock을 반환합니다.
     * @return 내부에서 사용하는 원본 Clock
     */
    public Clock unwrap() { return clock; }
}
