package com.cpf.foundation.time;
import static org.assertj.core.api.Assertions.assertThat; import java.time.*; import org.junit.jupiter.api.Test;
class CpfClockTest { @Test void fixedClockIsDeterministic(){ var instant=Instant.parse("2026-08-09T00:00:00Z"); var clock=CpfClock.fixed(instant, ZoneOffset.UTC); assertThat(clock.instant()).isEqualTo(instant); assertThat(clock.today()).isEqualTo(LocalDate.of(2026,8,9)); } }
