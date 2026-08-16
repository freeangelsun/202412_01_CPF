package com.cpf.testkit.fixture;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/** multi-instance fan-out/reconcile 테스트용 in-process harness. */
public final class CpfMultiInstanceHarness<T> {
    private final ConcurrentHashMap<String, Consumer<T>> instances = new ConcurrentHashMap<>();

    public void register(String instanceId, Consumer<T> consumer) {
        String key = normalize(instanceId);
        if (instances.putIfAbsent(key, Objects.requireNonNull(consumer, "consumer")) != null) {
            throw new IllegalStateException("duplicate instanceId: " + key);
        }
    }

    public void unregister(String instanceId) { instances.remove(normalize(instanceId)); }
    public int size() { return instances.size(); }

    public List<String> broadcast(T event) {
        List<String> delivered = new ArrayList<>();
        instances.entrySet().stream().sorted(java.util.Map.Entry.comparingByKey()).forEach(entry -> {
            entry.getValue().accept(event);
            delivered.add(entry.getKey());
        });
        return List.copyOf(delivered);
    }

    private static String normalize(String id) {
        String value = Objects.requireNonNull(id, "instanceId").trim();
        if (value.isEmpty()) throw new IllegalArgumentException("instanceId must not be blank");
        return value;
    }
}
