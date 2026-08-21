package com.cpf.foundation.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/** Micrometer @Timed naming contract를 따르는 CPF performance annotation. Runtime은 CPF context tags를 추가한다. */
@Deprecated(forRemoval = false)
@Documented @Inherited @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.TYPE,ElementType.METHOD})
public @interface CpfTimed {
    String value() default "";
    String[] extraTags() default {};
    boolean longTask() default false;
    double[] percentiles() default {};
    boolean histogram() default false;
    String description() default "";
}
