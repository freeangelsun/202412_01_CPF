package com.cpf.web.runtime;

import com.cpf.web.internal.web.CpfResponseHeaderAdvice;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * Web Profile의 부가 HTTP runtime을 연결합니다.
 *
 * <p>Context root는 {@link CpfWebContextAutoConfiguration}, 오류 경계는
 * {@link CpfWebErrorAutoConfiguration}이 각각 단일 Owner로 처리합니다.
 * 과거 Core Transaction filter/interceptor를 다시 component-scan하지 않습니다.</p>
 */
@AutoConfiguration
@ConditionalOnClass(ResponseBodyAdvice.class)
@Import(CpfResponseHeaderAdvice.class)
public class CpfWebRuntimeAutoConfiguration {}
