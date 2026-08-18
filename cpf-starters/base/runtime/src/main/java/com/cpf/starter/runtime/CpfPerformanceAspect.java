package com.cpf.starter.runtime;

import io.micrometer.core.instrument.MeterRegistry;
import org.aspectj.lang.ProceedingJoinPoint;

/** @deprecated 내부 이전 이름입니다. Runtime Bean은 CpfTimedAspect 하나만 사용합니다. */
@Deprecated(forRemoval = true)
public final class CpfPerformanceAspect {
    private final CpfTimedAspect delegate;
    public CpfPerformanceAspect(CpfStarterProperties properties, MeterRegistry meterRegistry) {
        this.delegate = new CpfTimedAspect(properties, meterRegistry);
    }
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable { return delegate.around(joinPoint); }
}
