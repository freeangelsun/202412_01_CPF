package com.cpf.starter.runtime;

import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

/** Catalog packageBase를 사용해 Spring Bean의 Capability 사용을 자동 추적합니다. */
@Aspect
@Order(Ordered.LOWEST_PRECEDENCE - 200)
public final class CpfCapabilityUsageAspect {
    private final CpfRuntimeCapabilityInventory inventory;
    public CpfCapabilityUsageAspect(CpfRuntimeCapabilityInventory inventory) { this.inventory = inventory; }

    @Around("execution(public * com.cpf..*(..)) && !within(com.cpf.starter.runtime..*)")
    public Object track(ProceedingJoinPoint joinPoint) throws Throwable {
        Class<?> targetType = joinPoint.getTarget() == null
                ? ((MethodSignature) joinPoint.getSignature()).getDeclaringType()
                : org.springframework.aop.support.AopUtils.getTargetClass(joinPoint.getTarget());
        CpfRuntimeCapabilityDescriptor descriptor = inventory.resolveByClassName(targetType.getName());
        if (descriptor == null || !descriptor.operatorVisible()) return joinPoint.proceed();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        try (AutoCloseable ignored = CpfCapabilityUsageContext.bind(descriptor, method.getName())) {
            return joinPoint.proceed();
        }
    }
}
