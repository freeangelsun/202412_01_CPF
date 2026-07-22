package com.cpf.core.common.workflow;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 클래스 또는 메서드가 CPF 워크플로의 실행 단계임을 선언합니다.
 *
 * <p>실패 정책과 보상 거래 정보를 함께 지정해 분산 거래를 추적하고
 * 후속 복구 판단에 사용할 수 있도록 합니다.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CpfWorkflowStep {
    /** 워크플로 안에서 중복되지 않는 단계 식별자입니다. */
    String id() default "";

    /** 운영 화면과 로그에 표시할 단계 이름입니다. */
    String name() default "";

    /** 단계 실패 시 적용할 처리 정책입니다. */
    CpfWorkflowFailurePolicy failurePolicy() default CpfWorkflowFailurePolicy.FAIL;

    /** 이 단계가 보상 거래인지 여부입니다. */
    boolean compensation() default false;

    /** 현재 보상 거래를 식별하는 거래 ID입니다. */
    String compensationTransactionId() default "";

    /** 보상 대상이 되는 원거래 ID입니다. */
    String compensationTargetTransactionId() default "";
}

