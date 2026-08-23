package com.cpf.starter.runtime;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;
import org.springframework.aop.Pointcut;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.Ordered;
import org.springframework.context.annotation.Configuration;

/** Catalog packageBase에 실제 해당하는 proxy-safe 업무 Bean만 선별해 Capability 사용을 추적하는 Advisor입니다. */
public final class CpfCapabilityUsageAspect extends StaticMethodMatcherPointcutAdvisor {
    private final CpfRuntimeCapabilityInventory inventory;
    public CpfCapabilityUsageAspect(CpfRuntimeCapabilityInventory inventory) {
        this.inventory = java.util.Objects.requireNonNull(inventory, "inventory");
        setOrder(Ordered.LOWEST_PRECEDENCE - 200);
        setAdvice((MethodInterceptor) this::track);
    }

    /** Proxy 생성 전 정적 판정에서 infrastructure와 descriptor 비소유 Type을 제거합니다. */
    @Override
    public boolean matches(Method method, Class<?> targetType) {
        if (method == null || targetType == null || !Modifier.isPublic(method.getModifiers())) return false;
        Class<?> userType = org.springframework.util.ClassUtils.getUserClass(targetType);
        if (!proxySafeBusinessType(userType)) return false;
        CpfRuntimeCapabilityDescriptor descriptor = inventory.resolveByClassName(userType.getName());
        return descriptor != null && descriptor.operatorVisible();
    }

    private boolean proxySafeBusinessType(Class<?> type) {
        if (type.getName().startsWith("com.cpf.starter.runtime.")) return false;
        if (Modifier.isFinal(type.getModifiers())) return false;
        if (BeanPostProcessor.class.isAssignableFrom(type)
                || BeanFactoryPostProcessor.class.isAssignableFrom(type)
                || Advisor.class.isAssignableFrom(type)
                || Pointcut.class.isAssignableFrom(type)
                || AopInfrastructureBean.class.isAssignableFrom(type)) return false;
        if (AnnotatedElementUtils.hasAnnotation(type, Configuration.class)
                || AnnotatedElementUtils.hasAnnotation(type, ConfigurationProperties.class)) return false;
        return true;
    }

    private Object track(MethodInvocation invocation) throws Throwable {
        Class<?> targetType = invocation.getThis() == null
                ? invocation.getMethod().getDeclaringClass()
                : AopUtils.getTargetClass(invocation.getThis());
        CpfRuntimeCapabilityDescriptor descriptor = inventory.resolveByClassName(targetType.getName());
        if (descriptor == null || !descriptor.operatorVisible()) return invocation.proceed();
        try (AutoCloseable ignored = CpfCapabilityUsageContext.bind(descriptor, invocation.getMethod().getName())) {
            return invocation.proceed();
        }
    }
}
