package com.cpf.data.persistence.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Repository;

/** Spring {@link Repository}의 stereotype/bean-name semantics를 그대로 유지하는 CPF Repository Annotation입니다. */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repository
public @interface CpfRepository {
    @AliasFor(annotation = Repository.class, attribute = "value")
    String value() default "";
}
