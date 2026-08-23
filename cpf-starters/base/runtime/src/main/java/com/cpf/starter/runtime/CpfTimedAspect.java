package com.cpf.starter.runtime;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.annotation.CpfPerformance;
import com.cpf.foundation.annotation.CpfTimed;
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

/** {@code @CpfTimed}를 Micrometer Timer semantics로 계측하고 CPF context correlation을 추가합니다. */
@Aspect
public final class CpfTimedAspect {
    private static final Logger log = LoggerFactory.getLogger(CpfTimedAspect.class);
    private final CpfStarterProperties properties;
    private final MeterRegistry meterRegistry;

    public CpfTimedAspect(CpfStarterProperties properties, MeterRegistry meterRegistry) {
        this.properties = properties;
        this.meterRegistry = meterRegistry;
    }

    @Around("@annotation(com.cpf.foundation.annotation.CpfPerformance) || @within(com.cpf.foundation.annotation.CpfPerformance) || @annotation(com.cpf.foundation.annotation.CpfTimed) || @within(com.cpf.foundation.annotation.CpfTimed)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isPerformanceAnnotationEnabled()) return joinPoint.proceed();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        CpfPerformance performance = AnnotatedElementUtils.findMergedAnnotation(method, CpfPerformance.class);
        if (performance == null) performance = AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), CpfPerformance.class);
        CpfTimed legacy = null;
        if (performance == null) {
            legacy = AnnotatedElementUtils.findMergedAnnotation(method, CpfTimed.class);
            if (legacy == null) legacy = AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), CpfTimed.class);
        }
        if (performance == null && legacy == null) return joinPoint.proceed();

        String configuredValue = performance != null ? performance.value() : legacy.value();
        String operation = configuredValue.isBlank()
                ? method.getDeclaringClass().getSimpleName() + "." + method.getName()
                : configuredValue;
        long started = System.nanoTime();
        String outcome = "SUCCESS";
        try {
            return joinPoint.proceed();
        } catch (Throwable error) {
            outcome = "ERROR";
            throw error;
        } finally {
            long nanos = System.nanoTime() - started;
            Timer.Builder builder = Timer.builder("cpf.method.duration")
                    .tag("operation", operation)
                    .tag("outcome", outcome);
            String description = performance != null ? performance.description() : legacy.description();
            double[] percentiles = performance != null ? performance.percentiles() : legacy.percentiles();
            boolean histogram = performance != null ? performance.histogram() : legacy.histogram();
            String[] tags = performance != null ? performance.extraTags() : legacy.extraTags();
            if (!description.isBlank()) builder.description(description);
            if (percentiles.length > 0) builder.publishPercentiles(percentiles);
            if (histogram) builder.publishPercentileHistogram();
            if (tags.length % 2 != 0) {
                throw new IllegalStateException("CpfPerformance.extraTags must contain key/value pairs");
            }
            for (int i = 0; i < tags.length; i += 2) builder.tag(tags[i], tags[i + 1]);
            builder.register(meterRegistry).record(nanos, TimeUnit.NANOSECONDS);

            long elapsedMs = TimeUnit.NANOSECONDS.toMillis(nanos);
            long thresholdMs = properties.getPerformanceSlowThresholdMillis();
            if (elapsedMs >= thresholdMs) {
                log.warn("CPF SLOW operation={} tx={} exec={} elapsedMs={} thresholdMs={}", operation,
                        CpfContexts.currentTransactionId(), CpfContexts.currentExecutionId(), elapsedMs, thresholdMs);
            }
        }
    }
}
