package com.cpf.starter.runtime;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.annotation.CpfService;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;

/** @CpfService 호출이 관리 Context를 우회하지 못하도록 하는 경량 정책 Aspect입니다. */
@Aspect
public final class CpfServicePolicyAspect {
    private final CpfServicePolicyProperties properties;

    public CpfServicePolicyAspect(CpfServicePolicyProperties properties) {
        this.properties = properties;
    }

    @Around("@within(com.cpf.foundation.annotation.CpfService)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isEnabled()) return joinPoint.proceed();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        CpfService service = AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), CpfService.class);
        if (service != null && service.contextRequired() && CpfContexts.current() == null) {
            throw new IllegalStateException("Managed @CpfService call has no bound CPF Context: " + method);
        }
        return joinPoint.proceed();
    }
}
