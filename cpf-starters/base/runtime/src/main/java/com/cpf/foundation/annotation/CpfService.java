package com.cpf.foundation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Service;

/**
 * Spring {@link Service}의 stereotype/bean-name semantics를 그대로 유지하는 CPF Service Annotation입니다.
 * CPF 거래 Context 생성·검증은 Online Transaction/Domain boundary가 담당하며 Service Annotation 자체가 의미를 바꾸지 않습니다.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface CpfService {
    @AliasFor(annotation = Service.class, attribute = "value")
    String value() default "";
}
