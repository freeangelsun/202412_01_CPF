package com.cpf.foundation.annotation;

import com.cpf.foundation.api.CpfBaseService;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Service;

/**
 * CPF Business Service Golden Path를 표시하는 Annotation입니다.
 *
 * <p>단순 Spring {@link Service} 별칭이 아니라 Base Starter가 {@code CpfBaseService} 상속과
 * 관리 Context 존재를 검증하는 CPF Runtime 정책의 진입점입니다.</p>
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface CpfService {
    boolean contextRequired() default true;
}
