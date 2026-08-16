package com.cpf.integration.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Integration Resilience Engine의 retry policy를 선언합니다. UNKNOWN은 자동 재실행하지 않습니다. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface CpfRetry {
    int maxAttempts() default 3;
    long delayMillis() default 100;
    boolean reconcileUnknownOutcome() default false;
}
