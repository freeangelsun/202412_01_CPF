package com.cpf.security.api.annotation;
import java.lang.annotation.*;
/** 위험 동작 실행 전에 Owner 승인 완료 상태와 사유를 검증하도록 요구합니다. */
@Documented @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
public @interface CpfApprovalRequired {
    String action();
    int approvals() default 1;
    boolean reasonRequired() default true;
    int approvalIdParameterIndex() default -1;
    int reasonParameterIndex() default -1;
    String approvalIdParameter() default "approvalId";
    String reasonParameter() default "reason";
}
