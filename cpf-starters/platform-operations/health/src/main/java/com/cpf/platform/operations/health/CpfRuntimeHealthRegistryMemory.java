package com.cpf.platform.operations.health;

import com.cpf.platform.operations.api.health.CpfRuntimeHealth;
import com.cpf.platform.operations.api.health.CpfRuntimeHealthRegistry;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 단일 ADM 인스턴스용 기본 registry. 외부 저장소 Provider로 교체 가능하며 stale report overwrite를 차단합니다. */
public final class CpfRuntimeHealthRegistryMemory implements CpfRuntimeHealthRegistry {
    private final ConcurrentHashMap<String, CpfRuntimeHealth> values = new ConcurrentHashMap<>();
    @Override public void upsert(CpfRuntimeHealth snapshot) {
        if (snapshot == null) throw new IllegalArgumentException("snapshot required");
        values.compute(snapshot.instanceKey(), (key, current) -> current == null || !snapshot.observedAt().isBefore(current.observedAt()) ? snapshot : current);
    }
    @Override public Optional<CpfRuntimeHealth> find(String systemId, String instanceId) {
        return Optional.ofNullable(values.get(systemId + ":" + instanceId));
    }
    @Override public List<CpfRuntimeHealth> list() {
        var out = new ArrayList<>(values.values());
        out.sort(Comparator.comparing(value -> value.systemId()).thenComparing(value -> value.instanceId()));
        return List.copyOf(out);
    }
}
