package cpf.cmn.tlm.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface CmnTelegramField {
    /**
     * CPF 기능 설명입니다.
     */
    int order();

    /**
     * CPF 기능 설명입니다.
     */
    String name() default "";

    /**
     * CPF 기능 설명입니다.
     */
    int length();

    /**
     * CPF 기능 설명입니다.
     */
    CmnTelegramFieldType type() default CmnTelegramFieldType.STRING;

    /**
     * CPF 기능 설명입니다.
     */
    CmnTelegramAlign align() default CmnTelegramAlign.AUTO;

    /**
     * CPF 기능 설명입니다.
     */
    char padding() default '\0';

    /**
     * CPF 기능 설명입니다.
     */
    String defaultValue() default "";

    /**
     * CPF 기능 설명입니다.
     */
    int scale() default 0;

    /**
     * CPF 기능 설명입니다.
     */
    boolean trim() default true;
}

