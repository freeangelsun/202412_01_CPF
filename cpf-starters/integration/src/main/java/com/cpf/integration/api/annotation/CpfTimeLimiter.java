package com.cpf.integration.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/** Resilience4j @TimeLimiter naming semantics에 맞춘 CPF annotation. timeout 값은 canonical resilience config에서 관리한다. */
@Documented @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.TYPE,ElementType.METHOD})
public @interface CpfTimeLimiter {
    String name();
    String fallbackMethod() default "";
}
