package com.cpf.core.api.fixedlength;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DTO 필드 또는 record component와 고정길이 전문 필드를 연결합니다.
 *
 * <p>{@link #length()}는 문자 수가 아니라 전문 charset으로 인코딩한 byte 수입니다.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface CpfFixedLengthField {
    int order();

    String name() default "";

    int length();

    CpfFixedLengthFieldType type() default CpfFixedLengthFieldType.STRING;

    CpfFixedLengthAlignment alignment() default CpfFixedLengthAlignment.AUTO;

    char padding() default '\0';

    String defaultValue() default "";

    int scale() default 0;

    boolean trim() default true;

    boolean required() default false;

    boolean sensitive() default false;

    String converterId() default "";
}
