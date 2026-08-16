package com.cpf.web.runtime;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.web.api.CpfController;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/** @CpfController 요청이 CPF Context 없이 실행되는 false-green을 차단합니다. */
public final class CpfControllerContextInterceptor implements HandlerInterceptor {
    private final CpfControllerPolicyProperties properties;

    public CpfControllerContextInterceptor(CpfControllerPolicyProperties properties) {
        this.properties = properties;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!properties.isEnabled() || !(handler instanceof HandlerMethod method)) return true;
        CpfController annotation = AnnotatedElementUtils.findMergedAnnotation(method.getBeanType(), CpfController.class);
        if (annotation != null && annotation.contextRequired() && CpfContexts.current() == null) {
            throw new IllegalStateException("Managed @CpfController request has no bound CPF Context: " + method.getBeanType().getName());
        }
        return true;
    }
}
