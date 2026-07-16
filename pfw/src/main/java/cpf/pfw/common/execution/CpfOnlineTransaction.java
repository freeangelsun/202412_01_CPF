package cpf.pfw.common.execution;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** 온라인 Controller 또는 facade에 선언하는 CPF 표준 거래 메타데이터입니다. */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfOnlineTransaction {
    String id();

    String name();

    String ownerDomain() default "";

    String description() default "";

    String requiredPermission() default "";

    boolean auditReasonRequired() default false;

    String visibility() default "PUBLIC";

    boolean directAllowed() default true;

    boolean gatewayAllowed() default true;
}
