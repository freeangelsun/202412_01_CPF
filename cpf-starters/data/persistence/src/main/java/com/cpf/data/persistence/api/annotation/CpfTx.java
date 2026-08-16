package com.cpf.data.persistence.api.annotation;

import java.lang.annotation.*;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;

/**
 * CPF Persistence Transaction의 Canonical Annotation입니다.
 * TransactionManager/Propagation/Isolation을 명시적으로 표현해 Spring @Transactional 직접 사용을 Golden Path에서 제거합니다.
 */
@Documented
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfTx {
    String id();
    String name();
    String ownerDomain();
    String transactionManager() default "";
    Propagation propagation() default Propagation.REQUIRED;
    Isolation isolation() default Isolation.DEFAULT;
    String requiredPermission() default "";
    boolean auditReasonRequired() default false;
    String visibility() default "PUBLIC";
    boolean gatewayAllowed() default true;
    boolean directAllowed() default false;
    boolean readOnly() default false;
    int timeoutSeconds() default -1;
}
