package com.cpf.core.internal.observability;

import com.cpf.core.api.observability.CpfTelemetry;
import com.cpf.core.api.observability.CpfTraceContext;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

public final class DefaultCpfTelemetryHarness {
    private DefaultCpfTelemetryHarness() { }

    public static void main(String[] args) throws Exception {
        Clock clock = Clock.fixed(Instant.parse("2026-08-05T04:30:00Z"), ZoneOffset.UTC);
        verifiesBasicLifecycle(clock);
        verifiesConcurrentActiveLimit(clock);
        verifiesCloseStartRace(clock);
        verifiesCanonicalAttributeCollision(clock);
        System.out.println("CPF_DEFAULT_TELEMETRY_HARNESS_PASS");
    }

    private static void verifiesBasicLifecycle(Clock clock) {
        DefaultCpfTelemetry telemetry = new DefaultCpfTelemetry(clock, 1);
        CpfTraceContext context = CpfTraceContext.root("TX-TRACE-1", CpfTraceContext.SpanKind.REMOTE,
                "GET /members/123456", Map.of("cpf.module", "member"));
        CpfTelemetry.CpfTelemetrySpan span = telemetry.startSpan(context);
        check(number(telemetry.status(), "activeSpans") == 1L, "active span");
        expectRejected(() -> telemetry.startSpan(context), "active limit");
        span.error(new IllegalStateException("password=secret-value"));
        span.close();
        span.close();
        Map<String, Object> status = telemetry.status();
        check(number(status, "completedSpans") == 1L, "idempotent close");
        check(number(status, "errorSpans") == 1L, "error count");
        check("java.lang.IllegalStateException".equals(status.get("lastErrorType")), "error type only");
        check(!status.toString().contains("secret-value"), "error message not retained");
        expectFailure(() -> telemetry.startSpan("op", "REMOTE", Map.of("email", "user@example.com")),
                "PII attribute");
        telemetry.close();
        telemetry.close();
        expectFailure(() -> telemetry.startSpan("op", "LOCAL", Map.of()), "closed runtime");
        check("CLOSED".equals(telemetry.status().get("state")), "closed state");
        check(number(telemetry.status(), "activeSpans") == 0L, "closed active count");
        check("NOOP".equals(CpfTelemetry.noop().status().get("state")), "noop contract");
    }

    private static void verifiesConcurrentActiveLimit(Clock clock) throws Exception {
        int limit = 4;
        int workers = 64;
        DefaultCpfTelemetry telemetry = new DefaultCpfTelemetry(clock, limit);
        CountDownLatch ready = new CountDownLatch(workers);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger accepted = new AtomicInteger();
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < workers; i++) {
            Thread thread = new Thread(() -> {
                ready.countDown();
                await(start);
                CpfTelemetry.CpfTelemetrySpan span = null;
                try {
                    span = telemetry.startSpan("concurrent", "LOCAL", Map.of("cpf.module", "core"));
                    accepted.incrementAndGet();
                    await(release);
                } catch (RejectedExecutionException expected) {
                    // bounded rejection
                } finally {
                    if (span != null) span.close();
                }
            }, "cpf-telemetry-limit-" + i);
            threads.add(thread);
            thread.start();
        }
        ready.await();
        start.countDown();
        waitUntil(() -> accepted.get() >= limit || number(telemetry.status(), "rejectedSpans") > 0, 5_000L);
        check(accepted.get() <= limit, "active limit race accepted=" + accepted.get());
        check(number(telemetry.status(), "activeSpans") <= limit, "active status exceeds limit");
        release.countDown();
        for (Thread thread : threads) thread.join();
        check(number(telemetry.status(), "activeSpans") == 0L, "active permits leaked");
        telemetry.close();
    }

    private static void verifiesCloseStartRace(Clock clock) throws Exception {
        for (int round = 0; round < 50; round++) {
            DefaultCpfTelemetry telemetry = new DefaultCpfTelemetry(clock, 64);
            CountDownLatch start = new CountDownLatch(1);
            Thread producer = new Thread(() -> {
                await(start);
                for (int i = 0; i < 1_000; i++) {
                    try {
                        CpfTelemetry.CpfTelemetrySpan span = telemetry.startSpan(
                                "race", "LOCAL", Map.of("cpf.module", "core"));
                        span.close();
                    } catch (IllegalStateException | RejectedExecutionException closed) {
                        return;
                    }
                }
            }, "cpf-telemetry-producer");
            Thread closer = new Thread(() -> {
                await(start);
                telemetry.close();
            }, "cpf-telemetry-closer");
            producer.start();
            closer.start();
            start.countDown();
            producer.join();
            closer.join();
            Map<String, Object> status = telemetry.status();
            check("CLOSED".equals(status.get("state")), "race state");
            check(number(status, "activeSpans") == 0L, "start-after-close leaked active span");
        }
    }

    private static void verifiesCanonicalAttributeCollision(Clock clock) {
        DefaultCpfTelemetry telemetry = new DefaultCpfTelemetry(clock, 10);
        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("CPF.Module", "core");
        attributes.put("cpf.module", "duplicate");
        expectFailure(() -> telemetry.startSpan("op", "LOCAL", attributes),
                "canonical attribute collision");
        String secretKind = "secret-kind-password=raw";
        try {
            telemetry.startSpan("op", secretKind, Map.of());
            throw new AssertionError("invalid kind must fail");
        } catch (IllegalArgumentException expected) {
            check(!String.valueOf(expected.getMessage()).contains(secretKind), "invalid kind leaked input");
        }
        telemetry.close();
    }

    private static long number(Map<String, Object> status, String key) {
        return ((Number) status.get(key)).longValue();
    }

    private static void waitUntil(Check condition, long timeoutMillis) throws InterruptedException {
        long deadline = System.currentTimeMillis() + timeoutMillis;
        while (!condition.get() && System.currentTimeMillis() < deadline) Thread.sleep(5L);
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("interrupted", interrupted);
        }
    }

    private static void expectRejected(Runnable action, String label) {
        try { action.run(); throw new AssertionError(label + " must fail"); }
        catch (RejectedExecutionException expected) { }
    }
    private static void expectFailure(Runnable action, String label) {
        try { action.run(); throw new AssertionError(label + " must fail"); }
        catch (IllegalArgumentException | IllegalStateException expected) { }
    }
    private static void check(boolean value, String label) {
        if (!value) throw new AssertionError(label);
    }
    @FunctionalInterface private interface Check { boolean get(); }
}
