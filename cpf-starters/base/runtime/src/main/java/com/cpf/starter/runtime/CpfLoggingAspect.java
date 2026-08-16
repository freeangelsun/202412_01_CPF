package com.cpf.starter.runtime;

import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.error.CpfException;
import com.cpf.foundation.annotation.CpfLogMode;
import com.cpf.foundation.annotation.CpfLogging;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotatedElementUtils;

/**
 * @CpfLogging 전용 Runtime입니다. Payload는 명시적 allowlist + scalar + CPF masking을 모두 통과해야 기록됩니다.
 * 성능 계측과 감사 증적은 각각 @CpfPerformance/@CpfAudit가 소유합니다.
 */
@Aspect
public final class CpfLoggingAspect {
    private final CpfStarterProperties properties;
    public CpfLoggingAspect(CpfStarterProperties properties) { this.properties = properties; }

    @Around("@annotation(com.cpf.foundation.annotation.CpfLogging) || @within(com.cpf.foundation.annotation.CpfLogging)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!properties.isLoggingAnnotationEnabled()) return joinPoint.proceed();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        CpfLogging logging = AnnotatedElementUtils.findMergedAnnotation(method, CpfLogging.class);
        if (logging == null) logging = AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(), CpfLogging.class);
        if (logging == null || !logging.enabled() || logging.mode() == CpfLogMode.NONE) return joinPoint.proceed();

        Logger logger = LoggerFactory.getLogger(method.getDeclaringClass());
        String operation = logging.operation().isBlank() ? method.getName() : logging.operation();
        if (logging.mode() == CpfLogMode.ENTRY_EXIT) {
            logger.debug("CPF ENTRY operation={} tx={} exec={} args={}", operation,
                    CpfContexts.currentTransactionId(), CpfContexts.currentExecutionId(),
                    safeArguments(method, joinPoint.getArgs(), logging));
        } else {
            logger.debug("CPF CALL operation={} tx={} exec={}", operation,
                    CpfContexts.currentTransactionId(), CpfContexts.currentExecutionId());
        }
        try {
            Object result = joinPoint.proceed();
            if (logging.mode() == CpfLogMode.ENTRY_EXIT) {
                logger.debug("CPF EXIT operation={} tx={} exec={} result={}", operation,
                        CpfContexts.currentTransactionId(), CpfContexts.currentExecutionId(), safeResult(result, logging));
            }
            return result;
        } catch (Throwable error) {
            String code = error instanceof CpfException cpf && cpf.getErrorCode() != null
                    ? cpf.getErrorCode().getStatusCode() : "UNCLASSIFIED";
            logger.warn("CPF ERROR operation={} tx={} exec={} code={} errorType={}", operation,
                    CpfContexts.currentTransactionId(), CpfContexts.currentExecutionId(), code,
                    error.getClass().getName());
            throw error;
        }
    }

    private String safeArguments(Method method, Object[] args, CpfLogging logging) {
        if (!logging.includeArguments() || logging.allowlist().length == 0 || args == null || args.length == 0) return "[OFF]";
        Set<String> allowed = new HashSet<>(Arrays.asList(logging.allowlist()));
        var parameters = method.getParameters();
        StringBuilder out = new StringBuilder("{");
        boolean first = true;
        for (int i = 0; i < Math.min(parameters.length, args.length); i++) {
            String name = parameters[i].getName();
            if (!allowed.contains(name) || !isSafeScalar(args[i])) continue;
            if (!first) out.append(',');
            first = false;
            out.append(name).append('=').append(CpfLogMasking.mask(String.valueOf(args[i]), properties.getLogValueMaxLength()));
        }
        return out.append('}').toString();
    }

    private String safeResult(Object result, CpfLogging logging) {
        if (!logging.includeResult() || !isSafeScalar(result)) return "[OFF]";
        if (!Arrays.asList(logging.resultAllowlist()).contains("value")) return "[OFF]";
        return CpfLogMasking.mask(String.valueOf(result), properties.getLogValueMaxLength());
    }

    private static boolean isSafeScalar(Object value) {
        return value == null || value instanceof CharSequence || value instanceof Number
                || value instanceof Boolean || value instanceof Enum<?>;
    }

}
