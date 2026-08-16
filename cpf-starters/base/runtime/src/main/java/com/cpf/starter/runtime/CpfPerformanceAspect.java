package com.cpf.starter.runtime;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.annotation.CpfPerformance;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;

/** @CpfPerformance 전용 계측 Runtime. Payload는 기록하지 않고 duration/status만 Metric과 안전 로그로 남깁니다. */
@Aspect
public final class CpfPerformanceAspect {
    private static final Logger log = LoggerFactory.getLogger(CpfPerformanceAspect.class);
    private final CpfStarterProperties properties;
    private final MeterRegistry meterRegistry;
    public CpfPerformanceAspect(CpfStarterProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Around("@annotation(com.cpf.foundation.annotation.CpfPerformance) || @within(com.cpf.foundation.annotation.CpfPerformance)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isPerformanceAnnotationEnabled()) return joinPoint.proceed();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        CpfPerformance perf = AnnotatedElementUtils.findMergedAnnotation(method, CpfPerformance.class);
        if (perf == null) perf = AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), CpfPerformance.class);
        if (perf == null || !perf.enabled()) return joinPoint.proceed();
        String operation = perf.value().isBlank() ? method.getDeclaringClass().getSimpleName() + "." + method.getName() : perf.value();
        long started = System.nanoTime();
        String outcome = "SUCCESS";
        try {
            return joinPoint.proceed();
        } catch (Throwable error) {
            outcome = "ERROR";
            throw error;
        } finally {
            long nanos = System.nanoTime() - started;
            Timer.builder("cpf.method.duration").tag("operation", operation).tag("outcome", outcome)
                    .register(meterRegistry).record(nanos, TimeUnit.NANOSECONDS);
            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(nanos);
            if (elapsedMs >= perf.slowThresholdMillis()) {
                log.warn("CPF SLOW operation={} tx={} exec={} elapsedMs={} thresholdMs={}", operation,
                        CpfContexts.currentTransactionId(), CpfContexts.currentExecutionId(), elapsedMs, perf.slowThresholdMillis());
            }
        }
    }
}
