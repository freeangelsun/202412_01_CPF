package com.cpf.reliability.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CPF 관리 실행의 멱등성을 durable store와 결합하는 Runtime 정책입니다.
 * 단순 중복 호출 방지가 아니라 payload 충돌, 진행 중 실행, UNKNOWN, 결과 재생을 명시적으로 다룹니다.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface CpfIdempotent {
    String operation() default "";
    boolean required() default true;
    long ttlSeconds() default 86400;
    long inProgressTimeoutSeconds() default 300;
    boolean replayResult() default true;
}
