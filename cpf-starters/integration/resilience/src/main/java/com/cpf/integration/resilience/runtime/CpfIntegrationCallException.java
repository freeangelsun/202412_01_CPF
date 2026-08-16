package com.cpf.integration.resilience.runtime;

import com.cpf.integration.resilience.api.CpfResilienceOutcome;

/** 외부 연계의 비성공 Outcome을 손실 없이 표준화합니다. */
public final class CpfIntegrationCallException extends RuntimeException {
    private final String operationId; private final CpfResilienceOutcome.Status status; private final String reasonCode;
    public CpfIntegrationCallException(String operationId,CpfResilienceOutcome.Status status,String reasonCode){
        super("CPF integration call did not complete successfully: "+status+"/"+(reasonCode==null?"UNCLASSIFIED":reasonCode));
        this.operationId=operationId;this.status=status;this.reasonCode=reasonCode;
    }
    public String operationId(){return operationId;} public CpfResilienceOutcome.Status status(){return status;} public String reasonCode(){return reasonCode;}
}
