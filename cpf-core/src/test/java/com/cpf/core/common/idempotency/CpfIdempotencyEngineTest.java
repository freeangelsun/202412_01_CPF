package com.cpf.core.common.idempotency;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CpfIdempotencyEngineTest {

    @Test
    void sameKeyAndPayloadReplaysStoredResponse() {
        CpfIdempotencyEngine engine = engine();
        CpfIdempotencyCommand command = command("K1", "P1");

        CpfIdempotencyExecutionResult first = engine.execute(command, () -> "OK");
        CpfIdempotencyExecutionResult second = engine.execute(command, () -> "NOT_CALLED");

        assertThat(first.replayed()).isFalse();
        assertThat(second.replayed()).isTrue();
        assertThat(second.response()).isEqualTo("OK");
    }

    @Test
    void sameKeyAndDifferentPayloadIsRejected() {
        CpfIdempotencyEngine engine = engine();
        engine.execute(command("K2", "P1"), () -> "OK");

        assertThatThrownBy(() -> engine.execute(command("K2", "P2"), () -> "NO"))
                .isInstanceOf(CpfIdempotencyException.class)
                .extracting("code")
                .isEqualTo("CPF-IDEMPOTENCY-PAYLOAD-CONFLICT");
    }

    @Test
    void failedExecutionCanRetryAndThenReplay() {
        CpfIdempotencyEngine engine = engine();
        CpfIdempotencyCommand command = command("K3", "P1");

        assertThatThrownBy(() -> engine.execute(command, () -> {
            throw new IllegalStateException("실패");
        })).isInstanceOf(IllegalStateException.class);

        CpfIdempotencyExecutionResult retried = engine.execute(command, () -> "RECOVERED");
        CpfIdempotencyExecutionResult replayed = engine.execute(command, () -> "NO");

        assertThat(retried.replayed()).isFalse();
        assertThat(replayed.response()).isEqualTo("RECOVERED");
        assertThat(replayed.replayed()).isTrue();
    }

    @Test
    void unknownExecutionCanRetry() {
        CpfIdempotencyEngine engine = engine();
        CpfIdempotencyCommand command = command("K4", "P1");

        assertThatThrownBy(() -> engine.execute(command, () -> {
            throw new CpfUnknownResultException("결과 미확정");
        })).isInstanceOf(CpfUnknownResultException.class);

        assertThat(engine.execute(command, () -> "CONFIRMED").response()).isEqualTo("CONFIRMED");
    }

    @Test
    void concurrentDuplicateExecutesOperationOnlyOnce() throws Exception {
        CpfIdempotencyEngine engine = engine();
        CpfIdempotencyCommand command = command("K5", "P1");
        AtomicInteger executions = new AtomicInteger();
        CountDownLatch started = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Future<CpfIdempotencyExecutionResult> first = executor.submit(() -> engine.execute(command, () -> {
                executions.incrementAndGet();
                started.countDown();
                await(release);
                return "OK";
            }));
            started.await();

            Future<?> duplicate = executor.submit(() -> engine.execute(command, () -> "NO"));
            assertThatThrownBy(duplicate::get)
                    .hasRootCauseInstanceOf(CpfIdempotencyException.class)
                    .rootCause()
                    .extracting("code")
                    .isEqualTo("CPF-IDEMPOTENCY-IN-PROGRESS");

            release.countDown();
            assertThat(first.get().response()).isEqualTo("OK");
            assertThat(executions).hasValue(1);
        } finally {
            release.countDown();
            executor.shutdownNow();
        }
    }

    @Test
    void expiredProcessingRecordRestartsAfterProcessFailure() {
        Instant now = Instant.parse("2026-07-14T00:00:00Z");
        InMemoryCpfIdempotencyRepository repository = new InMemoryCpfIdempotencyRepository();
        CpfIdempotencyCommand command = command("K6", "P1");
        repository.reserve(new CpfIdempotencyRecord(
                command.scope(),
                command.idempotencyKey(),
                command.requestHash(),
                command.payloadHash(),
                CpfIdempotencyStatus.PROCESSING.name(),
                null,
                false,
                now.minusSeconds(120),
                null,
                now.minusSeconds(1)));
        CpfIdempotencyEngine engine = new CpfIdempotencyEngine(
                repository,
                Clock.fixed(now, ZoneOffset.UTC));

        CpfIdempotencyExecutionResult restarted = engine.execute(command, () -> "RESTARTED");

        assertThat(restarted.replayed()).isFalse();
        assertThat(restarted.response()).isEqualTo("RESTARTED");
    }

    private CpfIdempotencyEngine engine() {
        return new CpfIdempotencyEngine(new InMemoryCpfIdempotencyRepository());
    }

    private CpfIdempotencyCommand command(String key, String payload) {
        return new CpfIdempotencyCommand(
                "TEST",
                key,
                CpfIdempotencyEngine.sha256("POST:/test"),
                CpfIdempotencyEngine.sha256(payload),
                "TX-1",
                "SEG-1",
                Duration.ofMinutes(1));
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("테스트 대기 중 인터럽트가 발생했습니다.", ex);
        }
    }
}
