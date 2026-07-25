package com.cpf.core.common.saga;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 수동 복구 시 동일 Saga 정의를 다시 찾기 위한 Registry. */
public class CpfSagaDefinitionRegistry {
    private final Map<String,CpfSagaDefinition> definitions=new ConcurrentHashMap<>();
    public void register(CpfSagaDefinition d){CpfSagaDefinition old=definitions.putIfAbsent(d.sagaType(),d);if(old!=null&&old!=d)throw new IllegalStateException("Saga 정의 중복: "+d.sagaType());}
    public Optional<CpfSagaDefinition> find(String sagaType){return Optional.ofNullable(definitions.get(sagaType));}
}
