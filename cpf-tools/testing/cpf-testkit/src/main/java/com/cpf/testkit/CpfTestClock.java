package com.cpf.testkit;
import com.cpf.foundation.time.CpfClock; import java.time.*; import java.util.concurrent.atomic.AtomicReference;
public final class CpfTestClock { private final ZoneId zone; private final AtomicReference<Instant> now; public CpfTestClock(Instant start,ZoneId zone){this.zone=zone;this.now=new AtomicReference<>(start);} public CpfClock clock(){return CpfClock.fixed(now.get(),zone);} public Instant advance(Duration d){return now.updateAndGet(v->v.plus(d));} public Instant instant(){return now.get();} }
