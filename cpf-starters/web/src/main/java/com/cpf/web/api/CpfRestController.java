package com.cpf.web.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

/**
 * Spring {@link RestController} semantics를 그대로 유지하면서 CPF Web Runtime의 관리 대상임을 표시합니다.
 * CPF Context/거래 검증은 별도 Web Runtime이 처리하며 이 Annotation의 속성 의미를 변경하지 않습니다.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@RestController
public @interface CpfRestController {
    @AliasFor(annotation = Controller.class, attribute = "value")
    String value() default "";
}
