package com.cpf.core.common.web;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class TransactionWebMvcConfig implements WebMvcConfigurer {

    private final TransactionHeaderValidationInterceptor transactionHeaderValidationInterceptor;

    public TransactionWebMvcConfig(TransactionHeaderValidationInterceptor transactionHeaderValidationInterceptor) {
        this.transactionHeaderValidationInterceptor = transactionHeaderValidationInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(transactionHeaderValidationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/actuator/**",
                        "/error",
                        "/favicon.ico",
                        "/adm",
                        "/adm/",
                        "/adm/api/health",
                        "/bat/api/health",
                        "/webjars/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**");
    }
}

