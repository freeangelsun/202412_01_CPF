package com.cpf.admin.opr.audit;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class AdmMandatoryAuditWebConfig implements WebMvcConfigurer {
    private final AdmMandatoryAuditInterceptor interceptor;
    public AdmMandatoryAuditWebConfig(AdmMandatoryAuditInterceptor interceptor){this.interceptor=interceptor;}
    @Override public void addInterceptors(InterceptorRegistry registry){registry.addInterceptor(interceptor).addPathPatterns("/adm/api/**");}
}
