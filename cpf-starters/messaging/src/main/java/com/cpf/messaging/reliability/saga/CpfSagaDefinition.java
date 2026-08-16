package com.cpf.messaging.reliability.saga;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Saga type과 순서가 고정된 실행 Step의 불변 정의입니다. */
public record CpfSagaDefinition(String sagaType, List<CpfSagaStep> steps) {
    public CpfSagaDefinition {
        if (sagaType == null || sagaType.isBlank()) throw new IllegalArgumentException("sagaType 필수");
        sagaType = sagaType.trim();
        if (sagaType.length() > 80) throw new IllegalArgumentException("sagaType 길이 초과");
        steps = List.copyOf(steps == null ? List.of() : steps);
        if (steps.isEmpty()) throw new IllegalArgumentException("Saga step은 한 개 이상이어야 합니다.");
        Set<String> ids = new HashSet<>();
        for (CpfSagaStep step : steps) {
            if (step == null || step.stepId() == null || step.stepId().isBlank()) {
                throw new IllegalArgumentException("Saga stepId 필수");
            }
            if (!ids.add(step.stepId().trim())) throw new IllegalArgumentException("Saga stepId 중복");
        }
    }
}
