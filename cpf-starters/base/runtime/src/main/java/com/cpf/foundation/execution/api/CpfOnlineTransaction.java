package com.cpf.foundation.execution.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 온라인 거래의 안정적인 Canonical Operation Metadata입니다.
 * 개발자가 입력하는 값은 operationId, name, description이며 System/Domain/권한/호출정책은 Runtime/ADM이 소유합니다.
 */
@Documented
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface CpfOnlineTransaction {
    String operationId();
    String name();
    String description();
}
