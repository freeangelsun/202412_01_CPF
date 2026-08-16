package com.cpf.core.api.config;
import java.lang.annotation.*;
/** Typed configuration bean policy used by runtime tooling to avoid unsafe hot-reload assumptions. */
@Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) @Documented
/** CpfConfigPolicy 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public @interface CpfConfigPolicy { String prefix(); CpfConfigMutability mutability(); boolean secretSeparated() default false; }
