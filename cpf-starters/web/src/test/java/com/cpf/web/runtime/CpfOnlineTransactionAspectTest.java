package com.cpf.web.runtime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cpf.core.api.context.CpfContext;
import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

/** 온라인 거래 Annotation은 Operation metadata와 transactionId 연속성만 책임지는지 검증합니다. */
class CpfOnlineTransactionAspectTest {
    @Test void operationMetadataIsConsumedAndTransactionIdIsPreserved() throws Throwable {
        CpfContext context=context("tx-1");
        var aspect=new CpfOnlineTransactionAspect(()->context);
        ProceedingJoinPoint jp=joinPoint(Sample.class.getDeclaredMethod("plain"),new Sample(),"ok");
        assertEquals("ok",aspect.around(jp));
        verify(jp).proceed();
    }
    @Test void missingContextFailsClosed() throws Throwable {
        var aspect=new CpfOnlineTransactionAspect(()->null);
        assertThrows(NullPointerException.class,()->aspect.around(joinPoint(Sample.class.getDeclaredMethod("plain"),new Sample(),"x")));
    }
    @Test void mismatchedOperationContextFailsClosed() throws Throwable {
        CpfContext context=context("tx-1");
        var aspect=new CpfOnlineTransactionAspect(()->context);
        assertDoesNotThrow(() -> aspect.around(joinPoint(Sample.class.getDeclaredMethod("plain"),new Sample(),"x")));
    }
    private static ProceedingJoinPoint joinPoint(Method method,Object target,Object result) throws Throwable {
        ProceedingJoinPoint jp=mock(ProceedingJoinPoint.class);MethodSignature sig=mock(MethodSignature.class);
        when(jp.getSignature()).thenReturn(sig);when(sig.getMethod()).thenReturn(method);when(jp.getTarget()).thenReturn(target);when(jp.proceed()).thenReturn(result);return jp;
    }
    private static CpfContext context(String tx){
        Instant now=Instant.parse("2026-08-11T00:00:00Z");
        return new CpfContext(new CpfContext.CpfTransactionContext(
                tx,tx,null,null,null,
                "TEST","TEST",null,null,
                "API","API",null,null,
                LocalDate.of(2026,8,11),now,CpfContext.CpfTransactionOriginKind.HTTP,"TEST",tx),
                new CpfContext.CpfExecutionContext(null,"exec-1","exec-1",null,"seg-1",null,CpfContext.CpfExecutionType.API,1,0,now,null,CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),null,null,null);
    }
    static class Sample {
        @CpfOnlineTransaction(operationId="MBR_SAMPLE_TX",name="sample",description="sample 거래") Object plain(){return null;}
    }
}
