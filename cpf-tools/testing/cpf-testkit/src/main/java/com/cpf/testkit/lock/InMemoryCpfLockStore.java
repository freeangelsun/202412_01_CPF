package com.cpf.testkit.lock;

import com.cpf.data.lock.spi.CpfLockStore;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.UnaryOperator;

/**
 * Single-JVM bounded provider with the same optimistic CAS invariants as distributed providers.
 * Tombstone and fencing history is intentionally retained; once the configured key cardinality is
 * reached, creation of a new key fails closed instead of reusing a fencing epoch.
 */
public final class InMemoryCpfLockStore implements CpfLockStore {
    public static final int DEFAULT_MAXIMUM_TRACKED_KEYS = 100_000;
    private static final int MAXIMUM_ALLOWED_TRACKED_KEYS = 1_000_000;

    private final ConcurrentHashMap<String, StoredLock> locks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicLong> fences = new ConcurrentHashMap<>();
    private final Set<String> trackedKeys = new HashSet<>();
    private final Object mutationMonitor = new Object();
    private final int maximumTrackedKeys;
    private final Clock clock;
    private final AtomicLong capacityRejectionCount = new AtomicLong();
    private volatile Instant lastCapacityRejectionAt;

    public InMemoryCpfLockStore() {
        this(DEFAULT_MAXIMUM_TRACKED_KEYS, Clock.systemUTC());
    }

    public InMemoryCpfLockStore(int maximumTrackedKeys) {
        this(maximumTrackedKeys, Clock.systemUTC());
    }

    public InMemoryCpfLockStore(int maximumTrackedKeys, Clock clock) {
        if (maximumTrackedKeys < 1 || maximumTrackedKeys > MAXIMUM_ALLOWED_TRACKED_KEYS) {
            throw new IllegalArgumentException("maximumTrackedKeys must be between 1 and 1000000");
        }
        this.maximumTrackedKeys = maximumTrackedKeys;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public UpdateResult update(String key, UnaryOperator<StoredLock> transition) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(transition, "transition");
        synchronized (mutationMonitor) {
            reserveKey(key);
            Holder holder = new Holder();
            locks.compute(key, (ignored, before) -> {
                holder.before = before;
                StoredLock after = transition.apply(before);
                validateTransition(key, before, after);
                holder.after = after;
                return after;
            });
            return new UpdateResult(holder.before, holder.after);
        }
    }

    @Override
    public Optional<StoredLock> find(String key) {
        return Optional.ofNullable(locks.get(Objects.requireNonNull(key, "key")));
    }

    @Override
    public List<StoredLock> list(int limit) {
        int safe = Math.max(1, Math.min(limit, 1000));
        ArrayList<StoredLock> values = new ArrayList<>(locks.values());
        values.sort(Comparator.comparing(StoredLock::key));
        return List.copyOf(values.subList(0, Math.min(safe, values.size())));
    }

    @Override
    public long nextFence(String key) {
        Objects.requireNonNull(key, "key");
        synchronized (mutationMonitor) {
            reserveKey(key);
            AtomicLong sequence = fences.computeIfAbsent(key, ignored -> new AtomicLong());
            long current = sequence.get();
            if (current == Long.MAX_VALUE) {
                throw new IllegalStateException("fencing token exhausted for key");
            }
            long next = current + 1L;
            sequence.set(next);
            return next;
        }
    }

    @Override
    public CapacitySnapshot capacitySnapshot() {
        synchronized (mutationMonitor) {
            return new CapacitySnapshot(
                    true,
                    trackedKeys.size(),
                    maximumTrackedKeys,
                    capacityRejectionCount.get(),
                    lastCapacityRejectionAt);
        }
    }

    private void reserveKey(String key) {
        if (trackedKeys.contains(key)) return;
        if (trackedKeys.size() >= maximumTrackedKeys) {
            capacityRejectionCount.incrementAndGet();
            lastCapacityRejectionAt = clock.instant();
            throw new ResourceExhaustedException("in-memory lock key capacity exhausted");
        }
        trackedKeys.add(key);
    }

    private static void validateTransition(String key, StoredLock before, StoredLock after) {
        if (after == null) {
            throw new IllegalStateException("CpfLockStore transitions must retain a tombstone row");
        }
        if (!key.equals(after.key())) throw new IllegalStateException("lock transition changed the key");
        if (before == null) {
            if (after.rowVersion() != 1L) {
                throw new IllegalStateException("new lock rows must start at row version 1");
            }
            return;
        }
        if (Objects.equals(before, after)) return;
        if (before.rowVersion() == Long.MAX_VALUE || after.rowVersion() != before.rowVersion() + 1L) {
            throw new IllegalStateException("lock transition must increment row version exactly once");
        }
        if (after.fencingToken() < before.fencingToken()
                || after.ownerEpoch() < before.ownerEpoch()) {
            throw new IllegalStateException("lock epochs must be monotonic");
        }
        if (after.fencingToken() > before.fencingToken()
                && after.ownerEpoch() != after.fencingToken()) {
            throw new IllegalStateException("new lock owner epoch must equal the fencing epoch");
        }
    }

    private static final class Holder {
        private StoredLock before;
        private StoredLock after;
    }
}
