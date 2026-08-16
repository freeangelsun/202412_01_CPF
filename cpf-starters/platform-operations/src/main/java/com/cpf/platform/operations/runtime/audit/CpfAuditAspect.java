package com.cpf.platform.operations.runtime.audit;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.platform.operations.api.annotation.CpfAudit;
import com.cpf.platform.operations.api.audit.CpfAuditEvent;
import com.cpf.platform.operations.api.audit.CpfAuditReasonContext;
import com.cpf.platform.operations.api.audit.CpfAuditSink;
import java.lang.reflect.Method;
import java.time.Clock;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotatedElementUtils;

/** @CpfAudit 전용 Runtime. Logging/Performance payload를 재사용하지 않고 별도 append-only event만 기록합니다. */
@Aspect
public final class CpfAuditAspect {
    private final CpfAuditSink sink;
    private final CpfAuditProperties properties;
    private final Clock clock;
    public CpfAuditAspect(CpfAuditSink sink,CpfAuditProperties properties,Clock clock){
        this.sink=Objects.requireNonNull(sink);this.properties=Objects.requireNonNull(properties);this.clock=Objects.requireNonNull(clock);
    }

    @Around("@annotation(com.cpf.platform.operations.api.annotation.CpfAudit) || @within(com.cpf.platform.operations.api.annotation.CpfAudit)")
    public Object around(ProceedingJoinPoint jp) throws Throwable {
        if(!properties.isEnabled()) return jp.proceed();
        Method method=((MethodSignature)jp.getSignature()).getMethod();
        CpfAudit policy=AnnotatedElementUtils.findMergedAnnotation(method,CpfAudit.class);
        if(policy==null)policy=AnnotatedElementUtils.findMergedAnnotation(method.getDeclaringClass(),CpfAudit.class);
        if(policy==null)return jp.proceed();
        CpfContext context=CpfContexts.requireCurrent();
        String reason=CpfAuditReasonContext.current();
        if(policy.reasonRequired()&&(reason==null||reason.isBlank()))throw new IllegalStateException("CPF_AUDIT_REASON_REQUIRED:"+method);
        String eventId=UUID.randomUUID().toString();
        append(event(context,eventId,policy.action(),CpfAuditEvent.Phase.STARTED,reason,"STARTED",null,null));
        try{
            Object result=jp.proceed();
            append(event(context,eventId,policy.action(),CpfAuditEvent.Phase.COMPLETED,reason,"SUCCESS",null,
                    policy.includeSafeResultSummary()?safeSummary(result):null));
            return result;
        }catch(Throwable error){
            try{append(event(context,eventId,policy.action(),CpfAuditEvent.Phase.FAILED,reason,"FAILED",error.getClass().getName(),null));}
            catch(RuntimeException auditFailure){error.addSuppressed(auditFailure);}
            throw error;
        }
    }
    private void append(CpfAuditEvent event){
        try{sink.append(event);}catch(RuntimeException e){if(properties.isFailClosed())throw new IllegalStateException("CPF_AUDIT_PERSISTENCE_FAILED",e);}
    }
    private CpfAuditEvent event(CpfContext c,String id,String action,CpfAuditEvent.Phase phase,String reason,String outcome,String error,String summary){
        return new CpfAuditEvent(id,action,phase,c.transactionId(),c.executionId(),c.subjectId(),c.actorId(),reason,outcome,error,summary,clock.instant(),Map.of("segmentId",String.valueOf(c.segmentId())));
    }
    private String safeSummary(Object value){
        if(value==null)return "null";
        if(!(value instanceof CharSequence||value instanceof Number||value instanceof Boolean||value instanceof Enum<?>))return "[NON_SCALAR]";
        String s=String.valueOf(value).replaceAll("(?i)(password|secret|token|authorization)\\s*[=:]\\s*[^,;\\s]+","$1=***");
        return s.length()>properties.getSummaryMaxLength()?s.substring(0,properties.getSummaryMaxLength()):s;
    }
}
