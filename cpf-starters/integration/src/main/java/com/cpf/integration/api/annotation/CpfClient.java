package com.cpf.integration.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 외부 시스템 호출을 CPF Integration Context/Resilience/Error/관측 정책에 등록합니다.
 * Transport 구현 자체를 대체하지 않으며 HTTP/SOAP/TCP 등 실제 Adapter는 별도 Owner가 제공합니다.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CpfClient {
    String system();
    String operation() default "";
    boolean sideEffecting() default false;
    boolean contextRequired() default true;
}
