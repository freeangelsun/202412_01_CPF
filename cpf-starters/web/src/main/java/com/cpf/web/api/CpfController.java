package com.cpf.web.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.web.bind.annotation.RestController;

/**
 * CPF Web Controller Golden Path를 표시하는 Annotation입니다.
 *
 * <p>단순 {@link RestController} 별칭이 아닙니다. Web Runtime은 이 Annotation이 붙은 Bean이
 * {@link CpfBaseController} 3단 상속구조를 따르는지 검증하고, 요청 진입 시 CPF Context 존재를 fail-closed로 확인합니다.</p>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@RestController
public @interface CpfController {
    /** Context가 없는 비관리 요청을 허용할 특별한 경계에서만 false로 설정합니다. */
    boolean contextRequired() default true;
}
