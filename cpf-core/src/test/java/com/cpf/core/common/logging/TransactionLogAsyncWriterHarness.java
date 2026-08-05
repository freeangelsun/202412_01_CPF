package com.cpf.core.common.logging;

import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.api.observability.CpfTelemetry;
import com.cpf.core.common.logging.fallback.CpfTransactionLogFallbackPort;
import com.cpf.core.mapper.common.logging.TransactionLogMapper;
import com.cpf.core.service.common.logging.TransactionLogService;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/** Bounded async writer, queue saturation, immutable snapshot and duplicate suppression harness. */
public final class TransactionLogAsyncWriterHarness {
    private TransactionLogAsyncWriterHarness() {
    }

    public static void main(String[] args) throws Exception {
        CountDownLatch firstInsertStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstInsert = new CountDownLatch(1);
        AtomicInteger insertCount = new AtomicInteger();
        Set<String> persistedIdentities = ConcurrentHashMap.newKeySet();
        List<String> detailValues = java.util.Collections.synchronizedList(new ArrayList<>());
        TransactionLogMapper mapper = new TransactionLogMapper() {
            @Override
            public boolean existsRecoveryEvent(String recoveryEventId) {
                return persistedIdentities.contains(recoveryEventId);
            }

            @Override
            public void insertTransactionLog(TransactionLogRecord record) {
                int current = insertCount.incrementAndGet();
                if (current == 1) {
                    firstInsertStarted.countDown();
                    try {
                        if (!releaseFirstInsert.await(5, TimeUnit.SECONDS)) {
                            throw new IllegalStateException("first insert release timed out");
                        }
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("insert interrupted", interrupted);
                    }
                }
                if (!persistedIdentities.add(record.getRecoveryEventId())) {
                    throw new IllegalStateException("duplicate identity reached insert");
                }
                record.setLogIdx((long) current);
            }

            @Override
            public void insertTransactionLogDetail(Long logIdx, String key, String value, String auditUser) {
                if ("callerValue".equals(key)) {
                    detailValues.add(value);
                }
            }
        };
        TransactionLogService service = new TransactionLogService(mapper);
        AtomicInteger fallbackCount = new AtomicInteger();
        AtomicReference<String> fallbackIdentity = new AtomicReference<>();
        CpfTransactionLogFallbackPort fallback = (
                record, details, policy, failure) -> {
            fallbackCount.incrementAndGet();
            fallbackIdentity.set(record.getRecoveryEventId());
            return true;
        };
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T00:00:00Z"), ZoneOffset.UTC);
        TransactionLogListener listener = new TransactionLogListener(
                service,
                fallback,
                new TransactionLogListener.AsyncSettings(true, 1, 1, Duration.ofMillis(500)),
                clock);
        CountingTelemetry telemetry = new CountingTelemetry();
        listener.configureTelemetry(telemetry, new CpfTraceSamplingPolicy());

        Map<String, String> mutableDetails = new HashMap<>();
        mutableDetails.put("callerValue", "before-submit");
        TransactionLogRecord first = record("tx-async-1");
        TransactionLogRecord second = record("tx-async-2");
        TransactionLogRecord third = record("tx-async-3");
        listener.handleTransactionLogEvent(new TransactionLogEvent(first, mutableDetails, null));
        check(firstInsertStarted.await(5, TimeUnit.SECONDS), "first asynchronous insert did not start");
        mutableDetails.put("callerValue", "after-submit");
        listener.handleTransactionLogEvent(new TransactionLogEvent(second, Map.of(), null));
        listener.handleTransactionLogEvent(new TransactionLogEvent(third, Map.of(), null));

        var saturated = listener.snapshot();
        check(saturated.acceptedCount() == 2L, "worker and bounded queue must accept exactly two events");
        check(saturated.rejectedCount() == 1L, "third event must be rejected by the bounded queue");
        check(saturated.fallbackPreservedCount() == 1L && fallbackCount.get() == 1,
                "queue rejection must be durably preserved");
        check(CpfTransactionLogIdentity.valid(fallbackIdentity.get()),
                "fallback event must carry the shared canonical identity");

        releaseFirstInsert.countDown();
        awaitCompleted(listener, 2L);
        check(detailValues.equals(List.of("before-submit")),
                "caller-owned details must be snapshotted before asynchronous execution");

        listener.handleTransactionLogEvent(new TransactionLogEvent(second, Map.of(), null));
        awaitCompleted(listener, 3L);
        check(insertCount.get() == 2, "duplicate normal event must be suppressed before insert");
        check(listener.snapshot().terminalLossCount() == 0L, "durable queue fallback must prevent terminal loss");
        check(telemetry.started.get() == 3 && telemetry.closed.get() == 3,
                "DB log consumer must create and close one trace span per persistence attempt");
        check(telemetry.errors.get() == 0, "successful persistence must not mark trace errors");
        check("DEGRADED".equals(listener.snapshot().health()),
                "queue saturation must be visible through health without claiming data loss");
        listener.close();
        check(listener.snapshot().state() == com.cpf.core.api.logging.CpfAsyncLogWriterOperations.WriterState.CLOSED,
                "writer lifecycle must reach CLOSED");

        AtomicInteger syncFallbackCount = new AtomicInteger();
        TransactionLogListener synchronous = new TransactionLogListener(
                service,
                (record, details, policy, failure) -> {
                    syncFallbackCount.incrementAndGet();
                    return true;
                });
        synchronous.close();
        long insertsBeforeClosedSubmit = insertCount.get();
        synchronous.handleTransactionLogEvent(new TransactionLogEvent(record("tx-after-close"), Map.of(), null));
        check(insertCount.get() == insertsBeforeClosedSubmit,
                "closed synchronous compatibility listener must not persist new work");
        check(syncFallbackCount.get() == 1 && synchronous.snapshot().rejectedCount() == 1L,
                "closed synchronous listener must preserve rejected work in the durable fallback");

        AtomicInteger telemetryFallbackCount = new AtomicInteger();
        TransactionLogMapper failingMapper = new TransactionLogMapper() {
            @Override public boolean existsRecoveryEvent(String recoveryEventId) { return false; }
            @Override public void insertTransactionLog(TransactionLogRecord record) {
                throw new IllegalStateException("database unavailable secret=must-not-leak");
            }
            @Override public void insertTransactionLogDetail(
                    Long logIdx, String key, String value, String auditUser) { }
        };
        TransactionLogListener telemetryIsolated = new TransactionLogListener(
                new TransactionLogService(failingMapper),
                (record, details, policy, failure) -> {
                    telemetryFallbackCount.incrementAndGet();
                    return true;
                });
        ThrowingTelemetry throwingTelemetry = new ThrowingTelemetry();
        telemetryIsolated.configureTelemetry(throwingTelemetry, new CpfTraceSamplingPolicy());
        telemetryIsolated.handleTransactionLogEvent(new TransactionLogEvent(
                record("tx-telemetry-failure"), Map.of(), null));
        check(telemetryFallbackCount.get() == 1,
                "telemetry error/close failures must not skip durable DB fallback");
        check(telemetryIsolated.snapshot().completedCount() == 1L,
                "telemetry close failure must not corrupt completion accounting");
        check(telemetryIsolated.snapshot().terminalLossCount() == 0L,
                "successful fallback must prevent terminal loss despite telemetry failure");
        check(throwingTelemetry.errors.get() == 1 && throwingTelemetry.closes.get() == 1,
                "negative telemetry fixture must exercise error and close failures");
        telemetryIsolated.close();

        System.out.println("CPF_TRANSACTION_LOG_ASYNC_WRITER_HARNESS_PASS");
    }

    private static TransactionLogRecord record(String transactionId) {
        TransactionLogRecord record = new TransactionLogRecord();
        record.setTransactionId(transactionId);
        record.setSpanId("root");
        record.setLogType("FINAL");
        record.setSequenceNo(1);
        return record;
    }

    private static void awaitCompleted(TransactionLogListener listener, long expected) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (listener.snapshot().completedCount() < expected && System.nanoTime() < deadline) {
            Thread.sleep(10L);
        }
        check(listener.snapshot().completedCount() >= expected, "asynchronous writer did not complete in time");
    }

    private static final class ThrowingTelemetry implements CpfTelemetry {
        private final AtomicInteger errors = new AtomicInteger();
        private final AtomicInteger closes = new AtomicInteger();
        @Override public CpfTelemetrySpan startSpan(String name, String kind, Map<String, String> attributes) {
            return new CpfTelemetrySpan() {
                @Override public void error(Throwable throwable) {
                    errors.incrementAndGet();
                    throw new IllegalStateException("telemetry error provider unavailable");
                }
                @Override public void close() {
                    closes.incrementAndGet();
                    throw new IllegalStateException("telemetry close provider unavailable");
                }
            };
        }
        @Override public Map<String, Object> status() { return Map.of(); }
    }

    private static final class CountingTelemetry implements CpfTelemetry {
        private final AtomicInteger started = new AtomicInteger();
        private final AtomicInteger closed = new AtomicInteger();
        private final AtomicInteger errors = new AtomicInteger();
        @Override public CpfTelemetrySpan startSpan(String name, String kind, Map<String, String> attributes) {
            started.incrementAndGet();
            return new CpfTelemetrySpan() {
                private boolean done;
                @Override public void error(Throwable throwable) { errors.incrementAndGet(); }
                @Override public void close() { if (!done) { done = true; closed.incrementAndGet(); } }
            };
        }
        @Override public Map<String, Object> status() { return Map.of(); }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
