package com.cpf.data.persistence.api;

import java.lang.annotation.*;
import org.springframework.stereotype.Repository;

/**
 * CPF Persistence의 단일 Repository 표준 Annotation입니다.
 * Interface Port와 JDBC/MyBatis/JPA concrete Repository 모두 같은 이름으로 등록합니다.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repository
public @interface CpfRepository {
    /** 현재 CPF Context가 반드시 필요한 Repository인지 지정합니다. */
    boolean contextRequired() default true;
}
