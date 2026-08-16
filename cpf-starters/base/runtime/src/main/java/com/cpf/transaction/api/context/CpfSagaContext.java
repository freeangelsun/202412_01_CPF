package com.cpf.transaction.api.context;

/** Saga 실행과 보상 lineage를 표현하는 불변 Transaction 의미 값입니다. */
public record CpfSagaContext(
        String sagaId, String sagaType, String stepId, String compensationId,
        String state, int attempt, String originalExecutionId) {
    public CpfSagaContext {
        sagaId = required(sagaId, "sagaId", 180);
        sagaType = required(sagaType, "sagaType", 80);
        stepId = required(stepId, "stepId", 180);
        compensationId = optional(compensationId, 180);
        state = required(state, "state", 48);
        if (attempt < 1) throw new IllegalArgumentException("attempt는 1 이상이어야 합니다.");
        originalExecutionId = required(originalExecutionId, "originalExecutionId", 180);
    }
    private static String required(String v,String n,int m){String x=v==null?"":v.trim();if(x.isEmpty()||x.length()>m)throw new IllegalArgumentException(n+"은(는) 1~"+m+"자여야 합니다.");return x;}
    private static String optional(String v,int m){if(v==null)return null;String x=v.trim();if(x.isEmpty())return null;if(x.length()>m)throw new IllegalArgumentException("선택 식별자는 "+m+"자 이하여야 합니다.");return x;}
}
