package com.cpf.common.tlm.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** DTO 필드와 고정길이 전문 구간의 매핑 규칙을 선언합니다. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface CmnTelegramField {
    /** 전문에서 필드가 나타나는 순서입니다. 1부터 시작합니다. */
    int order();

    /** 전문 스키마에 표시할 필드명이며, 생략하면 Java 필드명을 사용합니다. */
    String name() default "";

    /** 문자 단위 고정 길이입니다. */
    int length();

    /** 문자열을 변환할 논리 자료형입니다. */
    CmnTelegramFieldType type() default CmnTelegramFieldType.STRING;

    /** 값과 채움 문자의 정렬 방식입니다. */
    CmnTelegramAlign align() default CmnTelegramAlign.AUTO;

    /** 명시적 채움 문자이며, NUL이면 자료형별 기본값을 사용합니다. */
    char padding() default '\0';

    /** 입력 값이 없을 때 사용할 기본 문자열입니다. */
    String defaultValue() default "";

    /** 소수점을 생략한 숫자 필드에서 적용할 소수 자릿수입니다. */
    int scale() default 0;

    /** 파싱 시 공백과 채움 문자를 제거할지 여부입니다. */
    boolean trim() default true;
}

