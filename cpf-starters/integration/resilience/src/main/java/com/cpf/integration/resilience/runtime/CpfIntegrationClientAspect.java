package com.cpf.integration.resilience.runtime;

import com.cpf.integration.api.annotation.*;
import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;

/** Developer Integration Annotation의 실제 AOP Consumer입니다. */
@Aspect
public final class CpfIntegrationClientAspect {
    private final CpfIntegrationClientCoordinator coordinator;
    public CpfIntegrationClientAspect(CpfIntegrationClientCoordinator coordinator){this.coordinator=coordinator;}
    @Around("@annotation(com.cpf.integration.api.annotation.CpfClient) || @within(com.cpf.integration.api.annotation.CpfClient)")
    public Object around(ProceedingJoinPoint jp)throws Throwable{
        Method m=((MethodSignature)jp.getSignature()).getMethod();
        CpfClient client=AnnotatedElementUtils.findMergedAnnotation(m,CpfClient.class);
        if(client==null)client=AnnotatedElementUtils.findMergedAnnotation(m.getDeclaringClass(),CpfClient.class);
        CpfRetry retry=AnnotatedElementUtils.findMergedAnnotation(m,CpfRetry.class); if(retry==null)retry=AnnotatedElementUtils.findMergedAnnotation(m.getDeclaringClass(),CpfRetry.class);
        CpfTimeout timeout=AnnotatedElementUtils.findMergedAnnotation(m,CpfTimeout.class); if(timeout==null)timeout=AnnotatedElementUtils.findMergedAnnotation(m.getDeclaringClass(),CpfTimeout.class);
        CpfTimeLimiter legacyTimeout=timeout==null?AnnotatedElementUtils.findMergedAnnotation(m,CpfTimeLimiter.class):null;
        if(timeout==null && legacyTimeout==null)legacyTimeout=AnnotatedElementUtils.findMergedAnnotation(m.getDeclaringClass(),CpfTimeLimiter.class);
        try{return coordinator.execute(m,jp.getArgs(),client,retry,timeout,legacyTimeout,()->{try{return jp.proceed();}catch(RuntimeException e){throw e;}catch(Exception e){throw e;}catch(Throwable t){throw new Wrapped(t);}});}
        catch(Wrapped w){throw w.getCause();}
    }
    private static final class Wrapped extends Exception{Wrapped(Throwable t){super(t);}}
}
