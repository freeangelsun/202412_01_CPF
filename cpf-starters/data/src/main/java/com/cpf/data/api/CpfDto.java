package com.cpf.data.api;

import java.lang.annotation.*;

/**
 * CPF DTO의 Validation/Masking/Generator Contract metadata입니다.
 * 단순 Spring/Jakarta alias가 아니며 Web/Generator/Testkit이 동일 metadata를 소비합니다.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface CpfDto {
    String contractVersion() default "1";
    boolean validationRequired() default true;
    boolean sensitive() default false;
}
