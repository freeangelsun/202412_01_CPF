package com.cpf.batch.runtime;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

/**
 * Scheduler, Worker, Center-Cut, Host Agent data plane이 매 실행 주기마다 직접 소비하는
 * immutable Batch Runtime 정책입니다.
 *
 * <p>실제 Consumer가 존재하는 정책만 보유합니다. 모든 변경은 하나의 CAS loop에서
 * version을 검증하므로 서로 다른 instance에서 늦게 도착한 과거 delivery가 최신 정책을
 * 역전 적용할 수 없습니다. 동일 version 재전달은 현재 값과 완전히 동일할 때만
 * idempotent replay로 허용합니다.</p>
 */
public final class BatchRuntimePolicy {
    public static final int MAX_CONCURRENCY = 10_000;

    private final AtomicReference<Snapshot> current = new AtomicReference<>(Snapshot.defaults());

    public Snapshot current() {
        return current.get();
    }

    public Snapshot replaceSchedule(long version, boolean enabled) {
        return replace(version, previous -> new Snapshot(
                version,
                enabled,
                previous.workerEnabled(),
                previous.workerConcurrencyLimit(),
                previous.calendarEnabled(),
                previous.centerCutEnabled(),
                previous.agentCommandsEnabled(),
                previous.agentLogCollectionEnabled()));
    }

    public Snapshot replaceConcurrency(long version, boolean workerEnabled, int concurrencyLimit) {
        requireRange("Batch worker concurrency", concurrencyLimit, MAX_CONCURRENCY);
        return replace(version, previous -> new Snapshot(
                version,
                previous.schedulerEnabled(),
                workerEnabled,
                concurrencyLimit,
                previous.calendarEnabled(),
                previous.centerCutEnabled(),
                previous.agentCommandsEnabled(),
                previous.agentLogCollectionEnabled()));
    }

    public Snapshot replaceCalendar(long version, boolean enabled) {
        return replace(version, previous -> new Snapshot(
                version,
                previous.schedulerEnabled(),
                previous.workerEnabled(),
                previous.workerConcurrencyLimit(),
                enabled,
                previous.centerCutEnabled(),
                previous.agentCommandsEnabled(),
                previous.agentLogCollectionEnabled()));
    }

    public Snapshot replaceCenterCut(long version, boolean enabled) {
        return replace(version, previous -> new Snapshot(
                version,
                previous.schedulerEnabled(),
                previous.workerEnabled(),
                previous.workerConcurrencyLimit(),
                previous.calendarEnabled(),
                enabled,
                previous.agentCommandsEnabled(),
                previous.agentLogCollectionEnabled()));
    }

    public Snapshot replaceAgentPolicy(
            long version,
            boolean commandsEnabled,
            boolean logCollectionEnabled) {
        return replace(version, previous -> new Snapshot(
                version,
                previous.schedulerEnabled(),
                previous.workerEnabled(),
                previous.workerConcurrencyLimit(),
                previous.calendarEnabled(),
                previous.centerCutEnabled(),
                commandsEnabled,
                logCollectionEnabled));
    }

    private void requireRange(String label, int value, int maximum) {
        if (value < 1 || value > maximum) {
            throw new IllegalArgumentException(label + "는 1~" + maximum + " 범위여야 합니다.");
        }
    }

    private Snapshot replace(long version, UnaryOperator<Snapshot> replacement) {
        if (version < 1) {
            throw new IllegalArgumentException("Batch runtime version은 1 이상이어야 합니다.");
        }
        while (true) {
            Snapshot previous = current.get();
            if (version < previous.version()) {
                throw new IllegalArgumentException(
                        "과거 Batch runtime version은 적용할 수 없습니다. current="
                                + previous.version() + ", requested=" + version);
            }
            Snapshot next = replacement.apply(previous);
            if (version == previous.version()) {
                if (next.equals(previous)) {
                    return previous;
                }
                throw new IllegalArgumentException(
                        "동일 Batch runtime version으로 다른 정책을 적용할 수 없습니다. version=" + version);
            }
            if (current.compareAndSet(previous, next)) {
                return next;
            }
        }
    }

    public record Snapshot(
            long version,
            boolean schedulerEnabled,
            boolean workerEnabled,
            int workerConcurrencyLimit,
            boolean calendarEnabled,
            boolean centerCutEnabled,
            boolean agentCommandsEnabled,
            boolean agentLogCollectionEnabled) {
        private static Snapshot defaults() {
            return new Snapshot(
                    0L,
                    true,
                    true,
                    MAX_CONCURRENCY,
                    true,
                    true,
                    true,
                    true);
        }
    }
}
