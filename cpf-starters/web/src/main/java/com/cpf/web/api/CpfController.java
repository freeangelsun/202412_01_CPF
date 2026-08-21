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
 * CPF 업무 Online Controller의 Canonical Annotation입니다.
 *
 * <p>Spring {@link RestController} semantics를 그대로 유지하며 CPF Web Runtime은 이 Annotation을 기준으로
 * Context/구조/DTO Validation 정책을 적용합니다.</p>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@RestController
public @interface CpfController {
    @AliasFor(annotation = Controller.class, attribute = "value")
    String value() default "";
}
