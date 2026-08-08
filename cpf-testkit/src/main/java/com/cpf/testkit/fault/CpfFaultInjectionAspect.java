package com.cpf.testkit.fault;

import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.reliability.CpfFaultInjector;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** 거래 ID Allowlist를 실제 실행 경계에 연결하는 검증 전용 소비 지점입니다. */
@Aspect
@Component
@Order(-50)
public class CpfFaultInjectionAspect {
    private final CpfFaultInjector injector;

    public CpfFaultInjectionAspect(CpfFaultInjector injector) {
        this.injector = injector;
    }

    @Around("@annotation(transaction)")
    public Object around(ProceedingJoinPoint joinPoint, CpfOnlineTransaction transaction) throws Throwable {
        injector.before(transaction.id());
        return joinPoint.proceed();
    }
}
