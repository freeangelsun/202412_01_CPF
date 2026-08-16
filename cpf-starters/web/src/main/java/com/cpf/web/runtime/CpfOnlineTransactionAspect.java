package com.cpf.web.runtime;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.web.api.CpfOnlineTransactionPolicyEvaluator;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

/** @CpfOnlineTransaction을 실제 Service/Controller 호출에서 소비하는 Runtime Aspect입니다. */
@Aspect
public final class CpfOnlineTransactionAspect {
    private final List<CpfOnlineTransactionPolicyEvaluator> evaluators;
    private final Supplier<CpfContext> contextSupplier;

    public CpfOnlineTransactionAspect(List<CpfOnlineTransactionPolicyEvaluator> evaluators) {
        this(evaluators, CpfContexts::requireCurrent);
    }

    CpfOnlineTransactionAspect(List<CpfOnlineTransactionPolicyEvaluator> evaluators, Supplier<CpfContext> contextSupplier) {
        this.evaluators = evaluators == null ? List.of() : List.copyOf(evaluators);
        this.contextSupplier = Objects.requireNonNull(contextSupplier, "contextSupplier");
    }

    @Around("execution(* *(..)) && (@annotation(com.cpf.foundation.annotation.CpfOnlineTransaction) || @within(com.cpf.foundation.annotation.CpfOnlineTransaction))")
    /** around 작업을 CPF 표준 계약에 따라 수행한다. */
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        CpfOnlineTransaction tx = resolve(joinPoint);
        if (tx == null) return joinPoint.proceed();
        CpfOnlineTransactionMetadataValidator.validate(tx, source(joinPoint));
        CpfContext before = Objects.requireNonNull(contextSupplier.get(), "CPF managed transaction requires bound context");
        String transactionId = requiredTransactionId(before);
        List<CpfOnlineTransactionPolicyEvaluator> supported = evaluators.stream()
                .filter(e -> e.supports(tx.ownerDomain())).toList();
        if (supported.size() > 1) {
            throw new IllegalStateException("CPF_ONLINE_TX_POLICY_EVALUATOR_AMBIGUOUS:" + tx.ownerDomain());
        }
        boolean securityPolicyRequired = !tx.requiredPermission().isBlank() || tx.auditReasonRequired();
        if (securityPolicyRequired && supported.isEmpty()) {
            throw new IllegalStateException("CPF_ONLINE_TX_POLICY_EVALUATOR_MISSING:" + tx.ownerDomain());
        }
        if (!supported.isEmpty()) supported.getFirst().verify(tx, before);
        try {
            return joinPoint.proceed();
        } finally {
            CpfContext after = Objects.requireNonNull(contextSupplier.get(), "CPF context disappeared during transaction");
            if (!transactionId.equals(requiredTransactionId(after))) {
                throw new IllegalStateException("CPF_TRANSACTION_ID_MUTATED:" + tx.id());
            }
        }
    }

    private static CpfOnlineTransaction resolve(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        CpfOnlineTransaction tx = AnnotatedElementUtils.findMergedAnnotation(method, CpfOnlineTransaction.class);
        if (tx != null) return tx;
        Object target = joinPoint.getTarget();
        Class<?> type = target == null ? method.getDeclaringClass() : ClassUtils.getUserClass(target);
        return AnnotatedElementUtils.findMergedAnnotation(type, CpfOnlineTransaction.class);
    }

    private static String source(ProceedingJoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        return method.getDeclaringClass().getName() + "#" + method.getName();
    }

    private static String requiredTransactionId(CpfContext context) {
        String id = context.transactionId();
        if (id == null || id.isBlank()) throw new IllegalStateException("CPF_ONLINE_TX_TRANSACTION_ID_REQUIRED");
        return id;
    }
}
