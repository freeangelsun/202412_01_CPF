package com.cpf.foundation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Micrometer naming/semantics를 따르는 CPF Canonical 성능 계측 Annotation입니다. */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CpfPerformance {
    String value() default "";
    String[] extraTags() default {};
    boolean longTask() default false;
    double[] percentiles() default {};
    boolean histogram() default false;
    String description() default "";
}
