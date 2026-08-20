package com.cpf.security.api.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.security.access.prepost.PreAuthorize;

/**
 * CPF 표준 권한 검증 Annotation입니다.
 *
 * <p>Spring Security {@link PreAuthorize}의 expression 의미를 그대로 사용하므로 별도 권한 DSL을
 * 만들지 않습니다. Controller/Service의 업무 권한은 이 Annotation을 Golden Path로 사용하고,
 * 실제 허용/차단 결정은 Spring Method Security가 수행합니다.</p>
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@PreAuthorize("")
public @interface CpfPermission {
    /** Spring Security authorization expression. */
    @AliasFor(annotation = PreAuthorize.class, attribute = "value")
    String value();
}
