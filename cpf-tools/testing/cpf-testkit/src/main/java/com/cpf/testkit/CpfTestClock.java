package com.cpf.testkit;
import com.cpf.foundation.time.CpfClock; import java.time.*; import java.util.concurrent.atomic.AtomicReference;
/**
 * CPF 시간 의존 코드를 결정적으로 검증하기 위한 Testkit clock입니다.
 * <p>테스트가 실제 시스템 시각을 기다리지 않고 동일한 {@code CpfClock} 계약에서 시각을 전진시킬 수 있게 합니다.
 */
public final class CpfTestClock { private final ZoneId zone; private final AtomicReference<Instant> now; public CpfTestClock(Instant start,ZoneId zone){this.zone=zone;this.now=new AtomicReference<>(start);} public CpfClock clock(){return CpfClock.fixed(now.get(),zone);} public Instant advance(Duration d){return now.updateAndGet(v->v.plus(d));} public Instant instant(){return now.get();} }
