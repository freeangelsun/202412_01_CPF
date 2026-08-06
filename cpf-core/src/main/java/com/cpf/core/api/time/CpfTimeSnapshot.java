package com.cpf.core.api.time;
import java.time.*;
public record CpfTimeSnapshot(Instant utc,ZoneId businessZone,ZonedDateTime businessTime,long monotonicNanos,Duration estimatedSkew,boolean healthy){
    public CpfTimeSnapshot { if(utc==null||businessZone==null||businessTime==null||estimatedSkew==null)throw new IllegalArgumentException("snapshot values required"); }
}
