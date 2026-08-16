package com.cpf.batch.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * Batch Job class를 CPF Job catalog/Context/운영 추적에 등록하는 정책 계약입니다.
 * Spring bean 등록만 제공하는 Alias가 아니라, Job 식별자/재시작/동시 실행 정책을 CPF Batch Runtime이 소비합니다.
 */
@Documented
@Component
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CpfBatchJob {
    String value();
    boolean restartable() default true;
    int maxConcurrentExecutions() default 1;
}
