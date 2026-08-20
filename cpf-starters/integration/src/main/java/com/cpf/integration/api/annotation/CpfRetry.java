package com.cpf.integration.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Resilience4j Retry의 name/fallbackMethod 계약을 따르는 CPF Retry Annotation입니다. 세부 retry 횟수/간격은 canonical config가 소유합니다. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface CpfRetry {
    String name();
    String fallbackMethod() default "";
    /** 1이면 retry 없음. 운영값은 환경 config에서 더 엄격하게 제한할 수 있습니다. */
    int maxAttempts() default 1;
    /** 재시도 사이 최소 지연(ms). */
    long delayMillis() default 0L;
    /** Side-effecting timeout/connection-loss UNKNOWN 결과를 reconcile queue로 넘길지 여부입니다. */
    boolean reconcileUnknownOutcome() default false;
}
