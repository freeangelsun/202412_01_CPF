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
}
