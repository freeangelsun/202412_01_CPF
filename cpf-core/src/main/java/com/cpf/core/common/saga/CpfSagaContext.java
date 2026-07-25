package com.cpf.core.common.saga;

import java.util.Map;

/** Saga 전체 Step이 공유하는 불변 실행 Context. */
public record CpfSagaContext(String sagaId,String sagaType,String businessKey,String transactionId,Map<String,Object> attributes) {
    public CpfSagaContext { attributes=attributes==null?Map.of():Map.copyOf(attributes); }
}
