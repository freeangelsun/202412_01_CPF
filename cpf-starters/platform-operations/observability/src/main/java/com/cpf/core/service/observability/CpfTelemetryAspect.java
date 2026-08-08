package com.cpf.core.service.observability;

import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.observability.CpfTelemetry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/** 기존 CPF 거래 Annotation을 실제 Telemetry consumer로 연결합니다. */
@Aspect
@Component
public class CpfTelemetryAspect {
    private final CpfTelemetry telemetry;

    public CpfTelemetryAspect(CpfTelemetry telemetry) {
        this.telemetry = telemetry;
    }

    @Around("@annotation(transaction)")
    public Object trace(ProceedingJoinPoint joinPoint, CpfOnlineTransaction transaction) throws Throwable {
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("cpf.transaction.definition.id", transaction.id());
        attributes.put("cpf.transaction.definition.name", transaction.name());
        String transactionId = CpfTransactionContext.transactionId();
        if (transactionId != null && !transactionId.isBlank()) attributes.put("cpf.transaction.id", transactionId);
        CpfTelemetry.CpfTelemetrySpan span = telemetry.startSpan(
                transaction.name().isBlank() ? transaction.id() : transaction.name(), "SERVER", attributes);
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            span.error(throwable);
            throw throwable;
        } finally {
            span.close();
        }
    }
}
