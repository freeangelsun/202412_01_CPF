package com.cpf.data.persistence.sql;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

/** transactionId별 read-after-write window와 Replica lag 기준을 보관합니다. */
public final class CpfReadRoutingRuntimePolicy {
    private static final int MAX_TRACKED_TRANSACTIONS = 100000;
    private final AtomicReference<Snapshot> snapshot = new AtomicReference<>(new Snapshot(true, 5000L, 3000L));
    private final ConcurrentHashMap<String, Long> lastWriteNanos = new ConcurrentHashMap<>();

    public Snapshot current() { return snapshot.get(); }

    public Snapshot replace(boolean enabled, long maxReplicaLagMillis, long readAfterWriteMillis) {
        if (maxReplicaLagMillis < 0 || maxReplicaLagMillis > 3_600_000L) {
            throw new IllegalArgumentException("maxReplicaLagMillis 범위 오류");
        }
        if (readAfterWriteMillis < 0 || readAfterWriteMillis > 600_000L) {
            throw new IllegalArgumentException("readAfterWriteMillis 범위 오류");
        }
        Snapshot next = new Snapshot(enabled, maxReplicaLagMillis, readAfterWriteMillis);
        snapshot.set(next);
        return next;
    }

    public void markWrite(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) return;
        if (lastWriteNanos.size() >= MAX_TRACKED_TRANSACTIONS) cleanup(System.nanoTime(), snapshot.get().readAfterWriteMillis());
        lastWriteNanos.put(transactionId, System.nanoTime());
    }

    public boolean readAfterWriteActive(String transactionId) {
        if (transactionId == null || transactionId.isBlank()) return false;
        Long writtenAt = lastWriteNanos.get(transactionId);
        if (writtenAt == null) return false;
        long windowNanos = snapshot.get().readAfterWriteMillis() * 1_000_000L;
        boolean active = System.nanoTime() - writtenAt <= windowNanos;
        if (!active) lastWriteNanos.remove(transactionId, writtenAt);
        return active;
    }

    private void cleanup(long nowNanos, long windowMillis) {
        long expiry = Math.max(windowMillis, 1000L) * 1_000_000L;
        for (Map.Entry<String, Long> entry : lastWriteNanos.entrySet()) {
            if (nowNanos - entry.getValue() > expiry) lastWriteNanos.remove(entry.getKey(), entry.getValue());
        }
    }

    /** Read routing 정책의 현재 version과 대상 정보를 원자적으로 전달하는 불변 Snapshot입니다. */
    public record Snapshot(boolean enabled, long maxReplicaLagMillis, long readAfterWriteMillis) {}
}
