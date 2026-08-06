package com.cpf.core.api.time;
import java.time.Duration;
public record CpfDeadline(long createdMonotonicNanos,long timeoutNanos){
    public CpfDeadline { if(timeoutNanos<0) throw new IllegalArgumentException("timeoutNanos must be >= 0"); }
    public static CpfDeadline after(CpfTimeSource source, Duration timeout){ if(timeout.isNegative())throw new IllegalArgumentException("timeout must not be negative"); return new CpfDeadline(source.monotonicNanos(),timeout.toNanos()); }
    public boolean expired(CpfTimeSource source){ return source.monotonicNanos()-createdMonotonicNanos>=timeoutNanos; }
}
