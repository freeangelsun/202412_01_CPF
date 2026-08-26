package com.cpf.batch.worker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.stereotype.Component;

/**
 * Spring Batch worker handler invocations and their admission capacity.
 *
 * <p>Kafka remains the transport and Spring Batch remains the execution engine. This class does
 * not claim or renew CPF database leases; it only prevents more business handler invocations than
 * the currently applied worker capacity while preserving already delivered Kafka work during a
 * drain.</p>
 */
@Component
public final class WorkerExecutionTracker {
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition capacityChanged = lock.newCondition();
    private final Map<ExecutionKey, Integer> pending = new HashMap<>();
    private final Map<ExecutionKey, Integer> active = new HashMap<>();
    private int capacityLimit = 1;
    private int pendingInvocations;
    private int activeInvocations;

    public Scope begin(String cpfExecutionId, long jobExecutionId) throws InterruptedException {
        return begin(cpfExecutionId, jobExecutionId, 0L);
    }

    public Scope begin(String cpfExecutionId, long jobExecutionId, long fencingToken)
            throws InterruptedException {
        ExecutionKey key = new ExecutionKey(
                normalize(cpfExecutionId, jobExecutionId), jobExecutionId, fencingToken);
        lock.lockInterruptibly();
        try {
            increment(pending, key);
            pendingInvocations++;
            try {
                while (activeInvocations >= capacityLimit) {
                    capacityChanged.await();
                }
            } catch (InterruptedException interrupted) {
                remove(pending, key);
                pendingInvocations--;
                capacityChanged.signalAll();
                throw interrupted;
            }
            remove(pending, key);
            pendingInvocations--;
            increment(active, key);
            activeInvocations++;
            return new Scope(this, key);
        } finally {
            lock.unlock();
        }
    }

    public void updateCapacity(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Worker capacity must be at least one");
        }
        lock.lock();
        try {
            capacityLimit = capacity;
            capacityChanged.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public Snapshot snapshot() {
        lock.lock();
        try {
            TreeSet<String> executionIds = new TreeSet<>();
            pending.keySet().forEach(key -> executionIds.add(key.cpfExecutionId()));
            active.keySet().forEach(key -> executionIds.add(key.cpfExecutionId()));
            Long currentJobExecutionId = allKeys().stream()
                    .map(value -> value.jobExecutionId())
                    .filter(id -> id > 0)
                    .min((left, right) -> left.compareTo(right))
                    .orElse(null);
            long fencingToken = allKeys().stream()
                    .mapToLong(value -> value.fencingToken())
                    .max()
                    .orElse(0L);
            return new Snapshot(
                    List.copyOf(executionIds),
                    currentJobExecutionId,
                    fencingToken,
                    capacityLimit,
                    activeInvocations,
                    pendingInvocations);
        } finally {
            lock.unlock();
        }
    }

    private List<ExecutionKey> allKeys() {
        ArrayList<ExecutionKey> keys = new ArrayList<>(pending.keySet());
        active.keySet().stream().filter(key -> !pending.containsKey(key)).forEach(keys::add);
        return keys;
    }

    private void complete(ExecutionKey key) {
        lock.lock();
        try {
            remove(active, key);
            activeInvocations--;
            capacityChanged.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private static void increment(Map<ExecutionKey, Integer> values, ExecutionKey key) {
        values.merge(key, 1, (left, right) -> Integer.sum(left, right));
    }

    private static void remove(Map<ExecutionKey, Integer> values, ExecutionKey key) {
        Integer count = values.get(key);
        if (count == null) {
            throw new IllegalStateException("Worker execution tracking is inconsistent");
        }
        if (count == 1) {
            values.remove(key);
        } else {
            values.put(key, count - 1);
        }
    }

    private static String normalize(String cpfExecutionId, long jobExecutionId) {
        String value = Objects.toString(cpfExecutionId, "").trim();
        return value.isEmpty() ? "spring-job-" + jobExecutionId : value;
    }

    private record ExecutionKey(String cpfExecutionId, long jobExecutionId, long fencingToken) {}

    public record Snapshot(
            List<String> executionIds,
            Long currentJobExecutionId,
            long fencingToken,
            int capacityLimit,
            int activeInvocations,
            int pendingInvocations) {
        public int inFlightInvocations() {
            return activeInvocations + pendingInvocations;
        }
    }

    public static final class Scope implements AutoCloseable {
        private final WorkerExecutionTracker owner;
        private final ExecutionKey key;
        private final AtomicBoolean closed = new AtomicBoolean();

        private Scope(WorkerExecutionTracker owner, ExecutionKey key) {
            this.owner = owner;
            this.key = key;
        }

        @Override
        public void close() {
            if (closed.compareAndSet(false, true)) {
                owner.complete(key);
            }
        }
    }
}
