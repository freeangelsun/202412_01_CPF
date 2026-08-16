package com.cpf.security.api.audit;
import java.time.Instant;
/** Permission/Approval 판단의 payload-free 보안 감사 이벤트입니다. */
public record CpfAuthorizationAuditEvent(String type,String action,String transactionId,String executionId,
        String subjectId,String actorId,boolean allowed,String reason,Instant occurredAt){
    public CpfAuthorizationAuditEvent{if(type==null||type.isBlank())throw new IllegalArgumentException("type");if(transactionId==null||transactionId.isBlank())throw new IllegalArgumentException("transactionId");if(occurredAt==null)throw new IllegalArgumentException("occurredAt");}
}
