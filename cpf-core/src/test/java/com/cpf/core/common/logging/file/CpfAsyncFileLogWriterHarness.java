package com.cpf.core.common.logging.file;

import com.cpf.core.api.logging.policy.LogPolicyDecision;
import com.cpf.core.common.logging.TransactionLogRecord;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/** bounded queue, overflow signal, caller-runs, drain shutdown and immutable prepared payload를 검증합니다. */
public final class CpfAsyncFileLogWriterHarness {
    private CpfAsyncFileLogWriterHarness() {
    }

    public static void main(String[] args) throws Exception {
        rejectsWithLossSignal();
        callerRunsWithoutLoss();
        rejectsPreparedPayloadAfterCloseWinsAdmission();
        preparedEventIsImmutable();
        System.out.println("CPF_ASYNC_FILE_LOG_WRITER_HARNESS_PASS");
    }

    private static void rejectsWithLossSignal() throws Exception {
        BlockingAdapter adapter = new BlockingAdapter();
        CpfAsyncFileLogWriter writer = new CpfAsyncFileLogWriter(
                adapter, 1, CpfAsyncFileLogWriter.OverflowPolicy.REJECT_WITH_LOSS_SIGNAL,
                Duration.ofSeconds(2));
        check(writer.publish(null, Map.of(), null) == CpfAsyncFileLogWriter.PublishResult.QUEUED, "first queued");
        check(adapter.workerEntered.await(2, TimeUnit.SECONDS), "worker entered");
        check(writer.publish(null, Map.of(), null) == CpfAsyncFileLogWriter.PublishResult.QUEUED, "second queued");
        check(writer.publish(null, Map.of(), null) == CpfAsyncFileLogWriter.PublishResult.REJECTED, "third rejected");
        adapter.release.countDown();
        writer.close();
        var snapshot = writer.snapshot();
        check(snapshot.writtenCount() == 2L, "queued logs drained");
        check(snapshot.rejectedCount() == 1L && snapshot.lossDetected(), "rejection signaled");
        var publicSnapshot = writer.fileWriterSnapshot();
        check(publicSnapshot.state() == com.cpf.core.api.logging.CpfAsyncFileLogWriterOperations.FileWriterState.CLOSED,
                "closed file writer state is publicly visible");
        check(publicSnapshot.terminalLossCount() == 1L && "DOWN".equals(publicSnapshot.health()),
                "terminal loss is publicly visible");
        check(writer.publish(null, Map.of(), null) == CpfAsyncFileLogWriter.PublishResult.CLOSED, "closed writer rejects");
    }

    private static void callerRunsWithoutLoss() throws Exception {
        BlockingAdapter adapter = new BlockingAdapter();
        CpfAsyncFileLogWriter writer = new CpfAsyncFileLogWriter(
                adapter, 1, CpfAsyncFileLogWriter.OverflowPolicy.CALLER_RUNS, Duration.ofSeconds(2));
        check(writer.publish(null, Map.of(), null) == CpfAsyncFileLogWriter.PublishResult.QUEUED, "first queued");
        check(adapter.workerEntered.await(2, TimeUnit.SECONDS), "worker entered");
        check(writer.publish(null, Map.of(), null) == CpfAsyncFileLogWriter.PublishResult.QUEUED, "second queued");
        check(writer.publish(null, Map.of(), null) == CpfAsyncFileLogWriter.PublishResult.CALLER_RAN, "caller runs");
        adapter.release.countDown();
        writer.close();
        var snapshot = writer.snapshot();
        check(snapshot.writtenCount() == 3L, "all logs written");
        check(snapshot.callerRunsCount() == 1L, "caller run counted");
        check(!snapshot.lossDetected(), "caller runs avoids loss");
        var publicSnapshot = writer.fileWriterSnapshot();
        check(publicSnapshot.terminalLossCount() == 0L, "caller-runs has no terminal loss");
        check("OUT_OF_SERVICE".equals(publicSnapshot.health()), "closed healthy writer is out of service");
    }

    private static void rejectsPreparedPayloadAfterCloseWinsAdmission() throws Exception {
        BlockingPrepareAdapter adapter = new BlockingPrepareAdapter();
        CpfAsyncFileLogWriter writer = new CpfAsyncFileLogWriter(
                adapter, 1, CpfAsyncFileLogWriter.OverflowPolicy.REJECT_WITH_LOSS_SIGNAL,
                Duration.ofSeconds(1));
        java.util.concurrent.atomic.AtomicReference<CpfAsyncFileLogWriter.PublishResult> result =
                new java.util.concurrent.atomic.AtomicReference<>();
        Thread publisher = new Thread(() -> result.set(writer.publish(null, Map.of(), null)),
                "cpf-file-log-publish-race");
        publisher.start();
        check(adapter.prepareEntered.await(2, TimeUnit.SECONDS), "prepare race fixture entered");
        writer.close();
        adapter.releasePrepare.countDown();
        publisher.join(2_000L);
        check(!publisher.isAlive(), "publisher must finish after close race");
        check(result.get() == CpfAsyncFileLogWriter.PublishResult.CLOSED,
                "payload prepared after close won admission must be rejected");
        check(adapter.writes.get() == 0, "closed admission must not write or enqueue the payload");
        check(writer.snapshot().queueDepth() == 0, "closed admission must leave no stranded queue item");
    }

    private static void preparedEventIsImmutable() {
        Map<String, Object> source = new java.util.LinkedHashMap<>();
        source.put("token", "masked");
        CpfFileLogWriter.PreparedFileLogEvent item = CpfFileLogWriter.PreparedFileLogEvent.transaction(
                "tx", LocalDate.of(2026, 8, 5), source);
        source.put("token", "changed");
        check("masked".equals(item.event().get("token")), "prepared event must snapshot source map");
        boolean immutable = false;
        try {
            item.event().put("new", "value");
        } catch (UnsupportedOperationException expected) {
            immutable = true;
        }
        check(immutable, "prepared event map must be immutable");
    }

    private static CpfFileLogWriter.PreparedTransactionLog prepared() {
        return new CpfFileLogWriter.PreparedTransactionLog(List.of(
                CpfFileLogWriter.PreparedFileLogEvent.transaction(
                        "tx", LocalDate.of(2026, 8, 5), Map.of("eventType", "TEST"))));
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class BlockingPrepareAdapter
            implements CpfAsyncFileLogWriter.PreparedLogAdapter {
        private final CountDownLatch prepareEntered = new CountDownLatch(1);
        private final CountDownLatch releasePrepare = new CountDownLatch(1);
        private final AtomicInteger writes = new AtomicInteger();

        @Override
        public CpfFileLogWriter.PreparedTransactionLog prepare(
                TransactionLogRecord record,
                Map<String, String> details,
                LogPolicyDecision policy) {
            prepareEntered.countDown();
            try {
                releasePrepare.await();
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("prepare interrupted", interrupted);
            }
            return prepared();
        }

        @Override
        public void write(CpfFileLogWriter.PreparedTransactionLog prepared) {
            writes.incrementAndGet();
        }
    }

    private static final class BlockingAdapter implements CpfAsyncFileLogWriter.PreparedLogAdapter {
        private final CountDownLatch workerEntered = new CountDownLatch(1);
        private final CountDownLatch release = new CountDownLatch(1);
        private final AtomicInteger writes = new AtomicInteger();

        @Override
        public CpfFileLogWriter.PreparedTransactionLog prepare(
                TransactionLogRecord record,
                Map<String, String> details,
                LogPolicyDecision policy) {
            return prepared();
        }

        @Override
        public void write(CpfFileLogWriter.PreparedTransactionLog prepared) {
            if (Thread.currentThread().getName().equals("cpf-file-log-writer")) {
                workerEntered.countDown();
                try {
                    release.await();
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("writer interrupted", interrupted);
                }
            }
            writes.incrementAndGet();
        }
    }
}
