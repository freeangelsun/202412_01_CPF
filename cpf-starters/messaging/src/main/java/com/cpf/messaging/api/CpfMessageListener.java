package com.cpf.messaging.api;
import java.lang.annotation.*;
/** Broker 종속 Annotation과 분리된 CPF Message Context/멱등/오류복구 Listener 정책입니다. */
@Documented @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
public @interface CpfMessageListener {
    String destination();
    String consumerGroup() default "";
    boolean idempotencyRequired() default true;
    boolean contextRequired() default true;
}
