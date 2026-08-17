package com.cpf.security.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.security.access.prepost.PreAuthorize;

/** Spring Security {@link PreAuthorize} expression semantics를 그대로 따르는 CPF Method Security Annotation입니다. */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@PreAuthorize("")
public @interface CpfPreAuthorize {
    @AliasFor(annotation = PreAuthorize.class, attribute = "value")
    String value();
}
