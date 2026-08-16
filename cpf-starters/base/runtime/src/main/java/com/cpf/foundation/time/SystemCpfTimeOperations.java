package com.cpf.foundation.time;
import java.time.*;
import java.util.Objects;
import java.util.function.Supplier;

/** UTC wall clock plus monotonic deadline source and injectable NTP-skew probe. */
public final class SystemCpfTimeOperations implements CpfTimeOperations {
    private final Clock clock; private final Supplier<Duration> skewProbe;
    public SystemCpfTimeOperations(){this(Clock.systemUTC(),()->Duration.ZERO);} public SystemCpfTimeOperations(Clock clock,Supplier<Duration> skewProbe){this.clock=Objects.requireNonNull(clock);this.skewProbe=Objects.requireNonNull(skewProbe);}
    @Override public Instant now(){return clock.instant();} @Override public long monotonicNanos(){return System.nanoTime();}
    @Override public CpfTimeSnapshot snapshot(ZoneId zone,Duration maximumAllowedSkew){ Duration skew=Objects.requireNonNullElse(skewProbe.get(),Duration.ZERO).abs(); Instant utc=now(); return new CpfTimeSnapshot(utc,zone,utc.atZone(zone),monotonicNanos(),skew,skew.compareTo(maximumAllowedSkew)<=0); }
}
