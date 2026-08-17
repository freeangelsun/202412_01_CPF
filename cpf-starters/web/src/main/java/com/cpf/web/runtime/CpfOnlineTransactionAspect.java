package com.cpf.web.runtime;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Supplier;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

/** 거래 Metadata와 transactionId 연속성만 보장합니다. 권한/승인/호출정책은 각 Owner Runtime이 별도로 집행합니다. */
@Aspect
public final class CpfOnlineTransactionAspect {
    private final Supplier<CpfContext> contextSupplier;
    public CpfOnlineTransactionAspect() { this(CpfContexts::requireCurrent); }
    CpfOnlineTransactionAspect(Supplier<CpfContext> contextSupplier) { this.contextSupplier = Objects.requireNonNull(contextSupplier); }

    @Around("execution(* *(..)) && (@annotation(com.cpf.foundation.execution.api.CpfOnlineTransaction) || @within(com.cpf.foundation.execution.api.CpfOnlineTransaction))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        CpfOnlineTransaction tx = resolve(joinPoint);
        if (tx == null) return joinPoint.proceed();
        CpfOnlineTransactionMetadataValidator.validate(tx, source(joinPoint));
        CpfContext before = Objects.requireNonNull(contextSupplier.get(), "CPF managed operation requires bound context");
        String transactionId = requiredTransactionId(before);
        if (before.operationId() != null && !before.operationId().isBlank() && !tx.operationId().equals(before.operationId())) {
            throw new IllegalStateException("CPF_OPERATION_CONTEXT_MISMATCH:" + tx.operationId() + ":" + before.operationId());
        }
        try { return joinPoint.proceed(); }
        finally {
            CpfContext after = Objects.requireNonNull(contextSupplier.get(), "CPF context disappeared during operation");
            if (!transactionId.equals(requiredTransactionId(after))) throw new IllegalStateException("CPF_TRANSACTION_ID_MUTATED:" + tx.operationId());
        }
    }
    private static CpfOnlineTransaction resolve(ProceedingJoinPoint joinPoint) {
        Method method=((MethodSignature)joinPoint.getSignature()).getMethod();
        CpfOnlineTransaction tx=AnnotatedElementUtils.findMergedAnnotation(method,CpfOnlineTransaction.class);
        if(tx!=null)return tx; Object target=joinPoint.getTarget();
        Class<?> type=target==null?method.getDeclaringClass():ClassUtils.getUserClass(target);
        return AnnotatedElementUtils.findMergedAnnotation(type,CpfOnlineTransaction.class);
    }
    private static String source(ProceedingJoinPoint joinPoint){Method method=((MethodSignature)joinPoint.getSignature()).getMethod();return method.getDeclaringClass().getName()+"#"+method.getName();}
    private static String requiredTransactionId(CpfContext context){String id=context.transactionId();if(id==null||id.isBlank())throw new IllegalStateException("CPF_ONLINE_TX_TRANSACTION_ID_REQUIRED");return id;}
}
