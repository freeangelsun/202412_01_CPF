package com.cpf.foundation.annotation;
import java.lang.annotation.*;
/** CPF 실행 Context와 연결된 성능 계측 정책입니다. Logging payload와 분리해 시간/결과만 기록합니다. */
@Documented @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.METHOD,ElementType.TYPE})
public @interface CpfPerformance {
    String value() default "";
    long slowThresholdMillis() default 1000;
    boolean enabled() default true;
}
