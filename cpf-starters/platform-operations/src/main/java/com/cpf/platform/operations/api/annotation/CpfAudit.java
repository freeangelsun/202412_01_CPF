package com.cpf.platform.operations.api.annotation;
import java.lang.annotation.*;
/** 변경/위험 작업의 불변 Audit event 생성을 요구합니다. Logging과 별도로 결과/행위/사유를 기록합니다. */
@Documented @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.TYPE,ElementType.METHOD})
public @interface CpfAudit {
    String action();
    boolean reasonRequired() default false;
    boolean includeSafeResultSummary() default false;
}
