package com.cpf.core.api.config;
import java.lang.annotation.*;
/** Typed configuration bean policy used by runtime tooling to avoid unsafe hot-reload assumptions. */
@Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) @Documented
public @interface CpfConfigPolicy { String prefix(); CpfConfigMutability mutability(); boolean secretSeparated() default false; }
