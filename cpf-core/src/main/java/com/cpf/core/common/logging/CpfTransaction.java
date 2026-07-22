package com.cpf.core.common.logging;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CPF 온라인/운영 API의 업무 거래 메타 정보를 선언합니다.
 *
 * <p>거래 로그, 표준 헤더 검증, OpenAPI 설명, ADM 관제에서 같은 거래 ID와 거래명을
 * 사용할 수 있도록 Controller 클래스 또는 메서드에 부여합니다.</p>
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfTransaction {
    /**
     * 업무 거래 ID입니다.
     *
     * <p>{모듈3자리}{업무구분2자리}{거래구분3자리}{일련번호4자리} 형식을 사용합니다.
     * 예: MBR01BSE0001.</p>
     */
    String id();

    /**
     * 운영자와 개발자가 식별할 수 있는 거래 논리명입니다.
     */
    String name();
}
