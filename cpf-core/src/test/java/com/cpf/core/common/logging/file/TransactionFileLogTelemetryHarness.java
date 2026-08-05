package com.cpf.core.common.logging.file;

import com.cpf.core.api.observability.CpfTelemetry;
import com.cpf.core.common.logging.CpfTraceSamplingPolicy;
import com.cpf.core.common.logging.TransactionLogEvent;
import com.cpf.core.common.logging.TransactionLogRecord;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public final class TransactionFileLogTelemetryHarness {
    private TransactionFileLogTelemetryHarness() { }

    public static void main(String[] args) {
        CpfAsyncFileLogWriter writer = new CpfAsyncFileLogWriter(
                new CpfAsyncFileLogWriter.PreparedLogAdapter() {
                    @Override public CpfFileLogWriter.PreparedTransactionLog prepare(
                            TransactionLogRecord record, Map<String, String> details,
                            com.cpf.core.api.logging.policy.LogPolicyDecision policy) {
                        return CpfFileLogWriter.PreparedTransactionLog.empty();
                    }
                    @Override public void write(CpfFileLogWriter.PreparedTransactionLog prepared) { }
                },
                2,
                CpfAsyncFileLogWriter.OverflowPolicy.CALLER_RUNS,
                Duration.ofSeconds(1));
        try {
            CountingTelemetry telemetry = new CountingTelemetry();
            TransactionFileLogListener listener = new TransactionFileLogListener(writer);
            listener.configureTelemetry(telemetry, new CpfTraceSamplingPolicy());
            TransactionLogRecord record = new TransactionLogRecord();
            record.setTransactionId("TX-FILE-TRACE-1");
            record.setModuleId("core");
            record.setStandardExecutionId("O-CORE-0001");
            record.setRequestType("FILE");
            record.setSequenceNo(1);
            TransactionLogEvent event = new TransactionLogEvent() {
                @Override public TransactionLogRecord getRecord() { return record; }
                @Override public Map<String, String> getDetails() { return Map.of(); }
                @Override public com.cpf.core.api.logging.policy.LogPolicyDecision getLogPolicy() { return null; }
            };
            listener.handleTransactionLogEvent(event);
            check(telemetry.started.get() == 1, "file consumer span start");
            check(telemetry.closed.get() == 1, "file consumer span close");
            check(telemetry.errors.get() == 0, "file consumer trace error");
        } finally {
            writer.close();
        }

        IllegalStateException fileFailure = new IllegalStateException("file prepare failed");
        CpfAsyncFileLogWriter failingWriter = new CpfAsyncFileLogWriter(
                new CpfAsyncFileLogWriter.PreparedLogAdapter() {
                    @Override public CpfFileLogWriter.PreparedTransactionLog prepare(
                            TransactionLogRecord record, Map<String, String> details,
                            com.cpf.core.api.logging.policy.LogPolicyDecision policy) {
                        throw fileFailure;
                    }
                    @Override public void write(CpfFileLogWriter.PreparedTransactionLog prepared) { }
                },
                2,
                CpfAsyncFileLogWriter.OverflowPolicy.CALLER_RUNS,
                Duration.ofSeconds(1));
        try {
            ThrowingTelemetry telemetry = new ThrowingTelemetry();
            TransactionFileLogListener listener = new TransactionFileLogListener(failingWriter);
            listener.configureTelemetry(telemetry, new CpfTraceSamplingPolicy());
            TransactionLogRecord record = record("TX-FILE-TRACE-FAIL");
            listener.handleTransactionLogEvent(event(record));
            check(telemetry.errors.get() == 1 && telemetry.closed.get() == 1,
                    "negative file telemetry fixture must exercise error and close failures");
            check(failingWriter.fileWriterSnapshot().failedCount() == 1L
                            && failingWriter.fileWriterSnapshot().terminalLossCount() == 1L,
                    "file prepare failure must be isolated from business flow and exposed as terminal loss");
        } finally {
            failingWriter.close();
        }

        CpfAsyncFileLogWriter successfulWriter = new CpfAsyncFileLogWriter(
                new CpfAsyncFileLogWriter.PreparedLogAdapter() {
                    @Override public CpfFileLogWriter.PreparedTransactionLog prepare(
                            TransactionLogRecord record, Map<String, String> details,
                            com.cpf.core.api.logging.policy.LogPolicyDecision policy) {
                        return CpfFileLogWriter.PreparedTransactionLog.empty();
                    }
                    @Override public void write(CpfFileLogWriter.PreparedTransactionLog prepared) { }
                },
                2,
                CpfAsyncFileLogWriter.OverflowPolicy.CALLER_RUNS,
                Duration.ofSeconds(1));
        try {
            TransactionFileLogListener listener = new TransactionFileLogListener(successfulWriter);
            listener.configureTelemetry(new CloseFailingTelemetry(), new CpfTraceSamplingPolicy());
            listener.handleTransactionLogEvent(event(record("TX-FILE-TRACE-CLOSE")));
        } finally {
            successfulWriter.close();
        }
        System.out.println("CPF_TRANSACTION_FILE_LOG_TELEMETRY_HARNESS_PASS");
    }

    private static TransactionLogRecord record(String transactionId) {
        TransactionLogRecord record = new TransactionLogRecord();
        record.setTransactionId(transactionId);
        record.setModuleId("core");
        record.setStandardExecutionId("O-CORE-0001");
        record.setRequestType("FILE");
        record.setSequenceNo(1);
        return record;
    }

    private static TransactionLogEvent event(TransactionLogRecord record) {
        return new TransactionLogEvent() {
            @Override public TransactionLogRecord getRecord() { return record; }
            @Override public Map<String, String> getDetails() { return Map.of(); }
            @Override public com.cpf.core.api.logging.policy.LogPolicyDecision getLogPolicy() { return null; }
        };
    }

    private static final class ThrowingTelemetry implements CpfTelemetry {
        private final AtomicInteger closed = new AtomicInteger();
        private final AtomicInteger errors = new AtomicInteger();
        @Override public CpfTelemetrySpan startSpan(String name, String kind, Map<String, String> attributes) {
            return new CpfTelemetrySpan() {
                @Override public void error(Throwable throwable) {
                    errors.incrementAndGet();
                    throw new IllegalStateException("telemetry error failed");
                }
                @Override public void close() {
                    closed.incrementAndGet();
                    throw new IllegalStateException("telemetry close failed");
                }
            };
        }
        @Override public Map<String, Object> status() { return Map.of(); }
    }

    private static final class CloseFailingTelemetry implements CpfTelemetry {
        @Override public CpfTelemetrySpan startSpan(String name, String kind, Map<String, String> attributes) {
            return new CpfTelemetrySpan() {
                @Override public void error(Throwable throwable) { }
                @Override public void close() {
                    throw new IllegalStateException("telemetry close failed");
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

    private static void check(boolean value, String label) {
        if (!value) throw new AssertionError(label);
    }
}
