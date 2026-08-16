package com.cpf.data.persistence.api;

import java.lang.annotation.*;
import org.springframework.stereotype.Repository;

/** Class 기반 JDBC/MyBatis DAO를 CPF Context/Exception/Diagnostics 정책 대상으로 등록합니다. */
@Documented @Target(ElementType.TYPE) @Retention(RetentionPolicy.RUNTIME) @Repository
public @interface CpfDao { boolean contextRequired() default true; }
