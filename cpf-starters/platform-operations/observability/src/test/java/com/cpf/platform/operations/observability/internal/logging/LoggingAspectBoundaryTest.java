package com.cpf.platform.operations.observability.internal.logging;

import org.aspectj.lang.annotation.Around;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Canonical business boundaries만 프록시하여 final 관리/transport Controller를 침범하지 않는지 검증합니다. */
class LoggingAspectBoundaryTest {
    @Test
    void observesAnnotatedTransactionsAndOfficialDomainOperationsOnly() throws Exception {
        Around around = LoggingAspect.class
                .getMethod("logTransaction", org.aspectj.lang.ProceedingJoinPoint.class)
                .getAnnotation(Around.class);
        String pointcut = around.value();

        assertTrue(pointcut.contains("CpfOnlineTransaction"));
        assertTrue(pointcut.contains("CpfDomainOperation+.invoke"));
        assertFalse(pointcut.contains("*Controller"));
    }
}
