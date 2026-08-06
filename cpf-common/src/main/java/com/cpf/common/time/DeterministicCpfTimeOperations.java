package com.cpf.common.time;
import com.cpf.core.api.time.*;
import java.time.*;
public final class DeterministicCpfTimeOperations implements CpfTimeOperations {
    private Instant instant; private long monotonic; private Duration skew=Duration.ZERO;
    public DeterministicCpfTimeOperations(Instant instant){this.instant=instant;}
    public void advance(Duration duration){instant=instant.plus(duration);monotonic=Math.addExact(monotonic,duration.toNanos());}
    public void setEstimatedSkew(Duration skew){this.skew=skew;}
    @Override public Instant now(){return instant;} @Override public long monotonicNanos(){return monotonic;}
    @Override public CpfTimeSnapshot snapshot(ZoneId zone,Duration max){return new CpfTimeSnapshot(instant,zone,instant.atZone(zone),monotonic,skew.abs(),skew.abs().compareTo(max)<=0);}
}
