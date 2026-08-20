package com.cpf.integration.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 외부 호출 Timeout 정책을 CPF Resilience Runtime에 연결하는 canonical annotation입니다.
 * timeoutMillis=0은 환경별 canonical resilience config 값을 사용한다는 의미이며 Source에 운영값을 강제하지 않습니다.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CpfTimeout {
    String name();
    long timeoutMillis() default 0L;
    String fallbackMethod() default "";
}
