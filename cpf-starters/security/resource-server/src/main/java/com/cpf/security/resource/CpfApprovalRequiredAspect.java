package com.cpf.security.resource;
import com.cpf.core.api.context.CpfContexts;import com.cpf.security.api.annotation.CpfApprovalRequired;import java.lang.reflect.Method;
import org.aspectj.lang.ProceedingJoinPoint;import org.aspectj.lang.annotation.*;import org.aspectj.lang.reflect.MethodSignature;import org.springframework.core.annotation.AnnotatedElementUtils;
/** @CpfApprovalRequired를 Owner 승인 검증 Port에 연결하며 Provider 부재도 fail-closed 합니다. */
@Aspect public final class CpfApprovalRequiredAspect{private final CpfApprovalCoordinator coordinator;public CpfApprovalRequiredAspect(CpfApprovalCoordinator c){coordinator=c;}
 @Around("@annotation(com.cpf.security.api.annotation.CpfApprovalRequired)") public Object around(ProceedingJoinPoint jp)throws Throwable{Method m=((MethodSignature)jp.getSignature()).getMethod();CpfApprovalRequired r=AnnotatedElementUtils.findMergedAnnotation(m,CpfApprovalRequired.class);coordinator.authorize(m,jp.getArgs(),r,CpfContexts.requireCurrent());return jp.proceed();}}
