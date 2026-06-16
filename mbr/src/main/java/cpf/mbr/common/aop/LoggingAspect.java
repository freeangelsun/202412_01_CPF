package cpf.mbr.common.aop;

import cpf.pfw.common.logging.SensitiveDataMasker;
import cpf.pfw.common.logging.TransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component("mbrLoggingAspect")
public class LoggingAspect {

    private static final long SLOW_SERVICE_THRESHOLD_MS = 1000L;

    @Around("execution(* cpf.mbr.service..*.*(..))")
    public Object logServicePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String transactionId = TransactionContext.getOrCreateTransactionId();
        String traceId = TransactionContext.getOrCreateTraceId();
        String methodName = joinPoint.getSignature().toShortString();

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;

            if (duration >= SLOW_SERVICE_THRESHOLD_MS) {
                log.warn(
                        "Slow service method. transactionId={}, traceId={}, method={}, durationMs={}",
                        transactionId,
                        traceId,
                        methodName,
                        duration);
            } else {
                log.debug(
                        "Service method completed. transactionId={}, traceId={}, method={}, durationMs={}",
                        transactionId,
                        traceId,
                        methodName,
                        duration);
            }

            return result;
        } catch (Throwable ex) {
            long duration = System.currentTimeMillis() - startTime;
            log.error(
                    "Service method failed. transactionId={}, traceId={}, method={}, durationMs={}, error={}",
                    transactionId,
                    traceId,
                    methodName,
                    duration,
                    SensitiveDataMasker.mask(ex.getMessage()),
                    ex);
            throw ex;
        }
    }
}

