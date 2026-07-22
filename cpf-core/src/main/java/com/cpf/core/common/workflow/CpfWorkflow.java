package com.cpf.core.common.workflow;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 클래스 또는 메서드가 CPF 워크플로 단위임을 선언합니다.
 *
 * <p>거래 로깅과 서비스 호출 계층은 이 메타데이터를 사용해 워크플로 식별자를
 * 하위 호출로 전파하고 운영 추적 정보를 구성합니다.</p>
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CpfWorkflow {
    /** 워크플로를 구분하는 안정적인 식별자입니다. */
    String id() default "";

    /** 운영 화면과 로그에 표시할 워크플로 이름입니다. */
    String name() default "";
}

