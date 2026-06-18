package cpf.pfw.common.logging;

import java.lang.annotation.Documented;
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
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfTransaction {
    /**
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * ?? MBR01BSE0001
     */
    String id();

    /**
     * CPF 기능 설명입니다.
     */
    String name();
}

