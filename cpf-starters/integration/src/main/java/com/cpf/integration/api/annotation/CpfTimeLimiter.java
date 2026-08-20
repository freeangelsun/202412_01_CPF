package com.cpf.integration.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/** @deprecated canonical 계약은 {@link CpfTimeout}입니다. 기존 Consumer의 단계적 migration만 지원합니다. */
@Deprecated(forRemoval = false)
@Documented @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.TYPE,ElementType.METHOD})
public @interface CpfTimeLimiter {
    String name();
    String fallbackMethod() default "";
}
