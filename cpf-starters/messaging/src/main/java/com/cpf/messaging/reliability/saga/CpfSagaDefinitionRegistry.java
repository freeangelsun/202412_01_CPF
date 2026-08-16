package com.cpf.messaging.reliability.saga;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/** 실행 중인 Saga가 동일 definition으로 보상될 수 있도록 type별 정의를 보관합니다. */
public final class CpfSagaDefinitionRegistry {
    private final ConcurrentMap<String, CpfSagaDefinition> definitions = new ConcurrentHashMap<>();

    public void register(CpfSagaDefinition definition) {
        if (definition == null) throw new IllegalArgumentException("Saga definition 필수");
        definitions.put(definition.sagaType(), definition);
    }

    public Optional<CpfSagaDefinition> find(String sagaType) {
        if (sagaType == null || sagaType.isBlank()) return Optional.empty();
        return Optional.ofNullable(definitions.get(sagaType.trim()));
    }
}
