package com.cpf.core.api.logging;

import java.time.Instant;

public final class CpfAsyncLogWriterOperationsHarness {
    public static void main(String[] args) {
        CpfAsyncLogWriterOperations.WriterSnapshot healthy =
                new CpfAsyncLogWriterOperations.WriterSnapshot(
                        true, 2, 100, 3, 1, 10, 6, 0, 0, 0,
                        null, null, CpfAsyncLogWriterOperations.WriterState.RUNNING);
        if (!"UP".equals(healthy.health()) || healthy.inFlightCount() != 4L) {
            throw new AssertionError("healthy snapshot");
        }
        CpfAsyncLogWriterOperations.WriterSnapshot degraded =
                new CpfAsyncLogWriterOperations.WriterSnapshot(
                        true, 1, 10, 0, 0, 5, 5, 1, 1, 0,
                        Instant.parse("2026-08-05T00:00:00Z"), null,
                        CpfAsyncLogWriterOperations.WriterState.DEGRADED);
        if (!"DEGRADED".equals(degraded.health())) throw new AssertionError("degraded health");
        CpfAsyncLogWriterOperations.WriterSnapshot down =
                new CpfAsyncLogWriterOperations.WriterSnapshot(
                        false, 1, 1, 0, 0, 1, 1, 0, 0, 1,
                        Instant.parse("2026-08-05T00:00:00Z"),
                        Instant.parse("2026-08-05T00:00:00Z"),
                        CpfAsyncLogWriterOperations.WriterState.CLOSED);
        if (!"DOWN".equals(down.health())) throw new AssertionError("terminal loss health");

        expectFailure(() -> new CpfAsyncLogWriterOperations.WriterSnapshot(
                true, 0, 10, 0, 0, 0, 0, 0, 0, 0,
                null, null, CpfAsyncLogWriterOperations.WriterState.RUNNING), "zero async workers");
        expectFailure(() -> new CpfAsyncLogWriterOperations.WriterSnapshot(
                true, 1, 10, 11, 0, 0, 0, 0, 0, 0,
                null, null, CpfAsyncLogWriterOperations.WriterState.RUNNING), "queue overflow metrics");
        expectFailure(() -> new CpfAsyncLogWriterOperations.WriterSnapshot(
                true, 1, 10, 0, 0, 1, 2, 0, 0, 0,
                null, null, CpfAsyncLogWriterOperations.WriterState.RUNNING), "completed exceeds accepted");
        expectFailure(() -> new CpfAsyncLogWriterOperations.WriterSnapshot(
                false, 1, 1, 0, 0, 1, 1, 0, 0, 1,
                null, null, CpfAsyncLogWriterOperations.WriterState.CLOSED), "missing loss timestamp");
        System.out.println("CPF_ASYNC_LOG_WRITER_SNAPSHOT_PASS");
    }

    private static void expectFailure(Runnable action, String label) {
        try {
            action.run();
            throw new AssertionError(label + " must fail");
        } catch (IllegalArgumentException | NullPointerException expected) {
            // expected
        }
    }
}
