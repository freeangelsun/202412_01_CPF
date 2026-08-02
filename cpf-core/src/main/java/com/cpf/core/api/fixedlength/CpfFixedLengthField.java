package com.cpf.core.api.fixedlength;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * DTO 필드 또는 record component와 고정길이 전문 필드를 연결합니다.
 *
 * <p>{@link #length()}는 문자 수가 아니라 전문 charset으로 인코딩한 byte 수입니다.</p>
 *
 * <p>Mapper는 강제 접근 권한 상승을 사용하지 않습니다. DTO 타입과 생성자는 public이어야 하며,
 * 일반 class의 non-public 필드는 같은 이름의 public JavaBean getter/setter를 제공해야 합니다.</p>
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
