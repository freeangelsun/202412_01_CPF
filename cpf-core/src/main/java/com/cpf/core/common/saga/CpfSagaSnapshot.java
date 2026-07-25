package com.cpf.core.common.saga;

import java.util.List;

public record CpfSagaSnapshot(String sagaId,String sagaType,String businessKey,String transactionId,CpfSagaStatus status,int version,String errorMessage,List<CpfSagaStepSnapshot> steps) {
    public CpfSagaSnapshot { steps=steps==null?List.of():List.copyOf(steps); }
}
