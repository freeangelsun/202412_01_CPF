package com.cpf.web.runtime;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** CPF Controller Context Interceptor를 MVC 호출 경로에 연결합니다. */
public final class CpfControllerPolicyWebMvcConfigurer implements WebMvcConfigurer {
    private final CpfControllerContextInterceptor interceptor;

    public CpfControllerPolicyWebMvcConfigurer(CpfControllerContextInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
    }
}
