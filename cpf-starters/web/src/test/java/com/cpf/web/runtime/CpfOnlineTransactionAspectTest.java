package com.cpf.web.runtime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.cpf.core.api.context.CpfContext;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.web.api.CpfOnlineTransactionPolicyEvaluator;
import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;

/** 온라인 거래 Annotation의 Runtime Consumer와 transactionId 보존을 검증합니다. */
class CpfOnlineTransactionAspectTest {
    @Test void policyAnnotationIsConsumedAndTransactionIdIsPreserved() throws Throwable {
        CpfContext context=context("tx-1");
        var evaluator=mock(CpfOnlineTransactionPolicyEvaluator.class); when(evaluator.supports("MBR")).thenReturn(true);
        var aspect=new CpfOnlineTransactionAspect(List.of(evaluator),()->context);
        ProceedingJoinPoint jp=joinPoint(Sample.class.getDeclaredMethod("secured"),new Sample(),"ok");
        assertEquals("ok",aspect.around(jp));
        verify(evaluator).verify(any(CpfOnlineTransaction.class),same(context));
        verify(jp).proceed();
    }
    @Test void missingContextFailsClosed() throws Throwable {
        var aspect=new CpfOnlineTransactionAspect(List.of(),()->null);
        assertThrows(NullPointerException.class,()->aspect.around(joinPoint(Sample.class.getDeclaredMethod("plain"),new Sample(),"x")));
    }
    @Test void declaredSecurityPolicyRequiresExactlyOneEvaluator() throws Throwable {
        CpfContext context=context("tx-1"); Method m=Sample.class.getDeclaredMethod("secured");
        var none=new CpfOnlineTransactionAspect(List.of(),()->context);
        assertThrows(IllegalStateException.class,()->none.around(joinPoint(m,new Sample(),"x")));
        var a=mock(CpfOnlineTransactionPolicyEvaluator.class);var b=mock(CpfOnlineTransactionPolicyEvaluator.class);
        when(a.supports("MBR")).thenReturn(true);when(b.supports("MBR")).thenReturn(true);
        var duplicate=new CpfOnlineTransactionAspect(List.of(a,b),()->context);
        assertThrows(IllegalStateException.class,()->duplicate.around(joinPoint(m,new Sample(),"x")));
    }
    private static ProceedingJoinPoint joinPoint(Method method,Object target,Object result) throws Throwable {
        ProceedingJoinPoint jp=mock(ProceedingJoinPoint.class);MethodSignature sig=mock(MethodSignature.class);
        when(jp.getSignature()).thenReturn(sig);when(sig.getMethod()).thenReturn(method);when(jp.getTarget()).thenReturn(target);when(jp.proceed()).thenReturn(result);return jp;
    }
    private static CpfContext context(String tx){
        Instant now=Instant.parse("2026-08-11T00:00:00Z");
        return new CpfContext(new CpfContext.CpfTransactionContext(tx,tx,null,null,null,"API","TEST",LocalDate.of(2026,8,11),now,CpfContext.CpfTransactionOriginKind.HTTP,"TEST",tx),
                new CpfContext.CpfExecutionContext(null,"exec-1","exec-1",null,"seg-1",null,CpfContext.CpfExecutionType.API,1,0,now,null,CpfContext.CpfCancellationMode.DEADLINE_ENFORCED),null,null,null);
    }
    /** Sample 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
    static class Sample {
        @CpfOnlineTransaction(id="MBR_SAMPLE_TX",name="sample",ownerDomain="MBR") Object plain(){return null;}
        @CpfOnlineTransaction(id="MBR_SECURED_TX",name="secured",ownerDomain="MBR",requiredPermission="MEMBER.WRITE",auditReasonRequired=true) Object secured(){return null;}
    }
}
