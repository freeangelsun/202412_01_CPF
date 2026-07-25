package com.cpf.core.common.saga;

import java.util.List;

public record CpfSagaDefinition(String sagaType,List<CpfSagaStep> steps) {
    public CpfSagaDefinition {
        if(sagaType==null||sagaType.isBlank())throw new IllegalArgumentException("sagaType은 필수입니다.");
        steps=steps==null?List.of():List.copyOf(steps);
        if(steps.isEmpty())throw new IllegalArgumentException("Saga step은 1개 이상이어야 합니다.");
        long distinct=steps.stream().map(CpfSagaStep::stepId).distinct().count();
        if(distinct!=steps.size())throw new IllegalArgumentException("Saga stepId는 중복될 수 없습니다.");
    }
}
