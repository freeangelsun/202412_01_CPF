package com.cpf.web.runtime;

import com.cpf.web.api.CpfBaseController;
import com.cpf.web.api.CpfRestController;
import com.cpf.foundation.api.CpfThreeTierStructurePolicy;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotatedElementUtils;

/** @CpfController가 3단 Base Class Golden Path를 우회하지 못하도록 시작 시 fail-fast 합니다. */
public final class CpfControllerPolicyBeanPostProcessor implements BeanPostProcessor {
    private final CpfControllerPolicyProperties properties;

    public CpfControllerPolicyBeanPostProcessor(CpfControllerPolicyProperties properties) {
        this.properties = properties;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (!properties.isEnabled() || !properties.isRequireBaseClass()) return bean;
        Class<?> type = AopUtils.getTargetClass(bean);
        if (AnnotatedElementUtils.hasAnnotation(type, CpfRestController.class)) {
            CpfThreeTierStructurePolicy.verifyBusinessType(type, CpfBaseController.class, "@CpfRestController");
        }
        return bean;
    }
}
