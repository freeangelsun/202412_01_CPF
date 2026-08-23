package com.cpf.reliability.runtime;

import com.cpf.reliability.api.CpfIdempotent;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;

/** {@code @CpfIdempotent}를 durable Coordinator로 연결하는 AOP Consumer입니다. */
@Aspect
public final class CpfIdempotencyAspect {
    private final CpfIdempotencyCoordinator coordinator;
    public CpfIdempotencyAspect(CpfIdempotencyCoordinator coordinator) { this.coordinator = coordinator; }

    @Around("@annotation(com.cpf.reliability.api.CpfIdempotent) || @within(com.cpf.reliability.api.CpfIdempotent)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        CpfIdempotent policy = AnnotatedElementUtils.findMergedAnnotation(method, CpfIdempotent.class);
        if (policy == null) policy = AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), CpfIdempotent.class);
        try {
            return coordinator.execute(method, joinPoint.getTarget(), joinPoint.getArgs(), policy, () -> {
                try { return joinPoint.proceed(); }
                catch (RuntimeException e) { throw e; }
                catch (Exception e) { throw e; }
                catch (Throwable t) { throw new WrappedThrowable(t); }
            });
        } catch (WrappedThrowable wrapped) {
            throw wrapped.getCause();
        }
    }
    private static final class WrappedThrowable extends Exception {
        WrappedThrowable(Throwable cause) { super(cause); }
    }
}
