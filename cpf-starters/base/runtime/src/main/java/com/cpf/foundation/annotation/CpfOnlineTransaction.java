package com.cpf.foundation.annotation;
import java.lang.annotation.*;
/** CPF 온라인/내부 거래 메타데이터 표준 Annotation입니다. Runtime policy는 Owner Starter가 해석합니다. */
@Target({ElementType.METHOD,ElementType.TYPE}) @Retention(RetentionPolicy.RUNTIME)
public @interface CpfOnlineTransaction { String id(); String name(); String ownerDomain(); String requiredPermission() default ""; boolean auditReasonRequired() default false; String visibility() default "PUBLIC"; boolean gatewayAllowed() default true; boolean directAllowed() default false; }
