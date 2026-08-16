package com.cpf.foundation.execution.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CPF 주제영역 간 내부 공유 API를 선언하는 공개 실행 계약입니다.
 *
 * <p>외부 Gateway 공개 대상이 아니며 CPF 서비스 신원/호출자 검증을 전제로 합니다.</p>
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfSharedApi {
    String id();
    String name();
    String ownerDomain() default "";
    String description() default "";
    String requiredPermission() default "";
    boolean auditReasonRequired() default false;
    String[] allowedCallers() default {};
}
