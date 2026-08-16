package com.cpf.batch.api.annotation;
import java.lang.annotation.*;
/** Batch Step method를 Job 내부의 안정된 step id와 연결하는 정책 계약입니다. */
@Documented @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
public @interface CpfBatchStep {
    String value();
    int order() default 0;
    boolean idempotent() default true;
}
