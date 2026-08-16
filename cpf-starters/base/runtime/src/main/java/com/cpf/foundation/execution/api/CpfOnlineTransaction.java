package com.cpf.foundation.execution.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 온라인/공유 API의 표준 실행 ID와 운영 메타데이터를 선언하는 공개 Annotation입니다.
 * Generator와 고객 Domain은 internal/common 실행 Annotation 대신 이 계약만 사용합니다.
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfOnlineTransaction {
    String id();
    String name();
    String ownerDomain() default "";
    String description() default "";
    String requiredPermission() default "";
    boolean auditReasonRequired() default false;
    String visibility() default "PUBLIC";
    boolean directAllowed() default true;
    boolean gatewayAllowed() default true;
}
