package com.cpf.starter.tcp;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class CpfTcpUnknownResultStore {
    private final ConcurrentHashMap<String, CpfTcpUnknownResult> values = new ConcurrentHashMap<>();
    private final int limit;

    public CpfTcpUnknownResultStore(int limit) {
        if (limit < 1) {
            throw new IllegalArgumentException("limit must be positive");
        }
        this.limit = limit;
    }

    public void record(CpfTcpUnknownResult value) {
        CpfTcpUnknownResult required = Objects.requireNonNull(value, "value must not be null");
        synchronized (values) {
            if (!values.containsKey(required.correlationId()) && values.size() >= limit) {
                throw new IllegalStateException("UNKNOWN_RESULT store limit reached");
            }
            values.put(required.correlationId(), required);
        }
    }

    public Optional<CpfTcpUnknownResult> find(String correlationId) {
        return Optional.ofNullable(values.get(correlationId));
    }

    public boolean reconcile(String correlationId) {
        return values.remove(correlationId) != null;
    }

    public List<CpfTcpUnknownResult> snapshot() {
        return values.values().stream()
                .sorted(Comparator.comparing(CpfTcpUnknownResult::writtenAt))
                .toList();
    }
}
