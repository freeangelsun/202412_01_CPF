package com.cpf.core.api.execution;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Batch/Worker 실행 메타데이터용 공개 Annotation입니다. Runtime 소유권은 cpf-batch에 둡니다. */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfBatchJob {
    String id();
    String name();
    String ownerDomain() default "";
    String description() default "";
    String requiredPermission() default "";
    boolean auditReasonRequired() default true;
}
