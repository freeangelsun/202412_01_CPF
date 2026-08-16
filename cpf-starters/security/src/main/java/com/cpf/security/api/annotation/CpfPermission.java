package com.cpf.security.api.annotation;
import java.lang.annotation.*;
/** 인증된 Principal의 CPF authority를 실제 Security Runtime에서 fail-closed로 검사합니다. */
@Documented @Retention(RetentionPolicy.RUNTIME) @Target({ElementType.TYPE,ElementType.METHOD})
public @interface CpfPermission {
    String[] value();
    boolean all() default true;
}
