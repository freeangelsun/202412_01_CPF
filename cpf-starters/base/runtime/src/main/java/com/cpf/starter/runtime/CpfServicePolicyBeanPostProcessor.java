package com.cpf.starter.runtime;

import com.cpf.foundation.annotation.CpfService;
import com.cpf.foundation.api.CpfBaseService;
import com.cpf.foundation.api.CpfThreeTierStructurePolicy;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;

/** @CpfService의 3단 Base Class 구조를 시작 시 검증합니다. */
public final class CpfServicePolicyBeanPostProcessor implements BeanPostProcessor {
    private final CpfServicePolicyProperties properties;

    public CpfServicePolicyBeanPostProcessor(CpfServicePolicyProperties properties) {
        this.properties = properties;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!properties.isEnabled() || !properties.isRequireBaseClass()) return bean;
        Class<?> type = AopUtils.getTargetClass(bean);
        if (AnnotatedElementUtils.hasAnnotation(type, CpfService.class)) {
            CpfThreeTierStructurePolicy.verifyBusinessType(type, CpfBaseService.class, "@CpfService");
        }
        return bean;
    }
}
