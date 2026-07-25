package com.cpf.batch.runtime.centercut;

import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Center-Cut 정의 Registry. 동일 jobId 중복 등록은 fail-fast 합니다. */
@Component
public class BatCenterCutRegistry {
    private final Map<String, BatCenterCutDefinition> definitions = new ConcurrentHashMap<>();

    public void register(BatCenterCutDefinition definition) {
        BatCenterCutDefinition previous = definitions.putIfAbsent(definition.jobId(), definition);
        if (previous != null && previous != definition) {
            throw new IllegalStateException("Center-Cut jobId가 이미 등록되어 있습니다: " + definition.jobId());
        }
    }

    public void unregister(String jobId) {
        if (jobId != null) definitions.remove(jobId);
    }

    public Optional<BatCenterCutDefinition> find(String jobId) {
        return Optional.ofNullable(definitions.get(jobId));
    }

    public List<Map<String,Object>> describe() {
        return definitions.values().stream()
                .sorted(Comparator.comparing(BatCenterCutDefinition::jobId))
                .map(d -> Map.<String,Object>of(
                        "jobId", d.jobId(),
                        "defaultLimit", d.defaultLimit(),
                        "maxLimit", d.maxLimit(),
                        "ratePerSecond", d.ratePerSecond()))
                .toList();
    }
}
