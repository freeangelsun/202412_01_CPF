package com.cpf.foundation.annotation;
import java.lang.annotation.*;
/** CPF Context correlation과 masking을 적용하는 안전한 애플리케이션 로깅 정책입니다. */
@Documented @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD,ElementType.TYPE})
public @interface CpfLogging {
    String operation() default "";
    CpfLogMode mode() default CpfLogMode.SUMMARY;
    boolean includeArguments() default false;
    String[] allowlist() default {};
    boolean includeResult() default false;
    String[] resultAllowlist() default {};
    boolean enabled() default true;
}
