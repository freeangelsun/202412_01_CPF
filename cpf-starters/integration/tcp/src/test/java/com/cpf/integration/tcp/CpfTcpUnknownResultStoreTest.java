package com.cpf.integration.tcp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class CpfTcpUnknownResultStoreTest {
    @Test
    void rejectsNewCorrelationWhenBoundIsReachedAndRejectsConflictingSameCorrelationRecord() {
        CpfTcpUnknownResultStore store = new CpfTcpUnknownResultStore(1);
        store.record(result("one", 1));

        // 같은 correlationId를 다른 내용으로 재기록하면 프로세스 간 correlationId 재사용 오류를
        // fail-closed로 막기 위해 항상 거부한다(reconciledCorrelationCannotBeRemovedByStaleVersionAfterRerecord
        // 처럼 reconcile() 이후 재기록만이 정상 update 경로다).
        IllegalStateException conflict = assertThrows(IllegalStateException.class,
                () -> store.record(result("one", 2)));
        assertTrue(conflict.getMessage().contains("correlation conflict"));
        assertArrayEquals(new byte[] {1}, store.find("one").orElseThrow().request());

        // 완전히 동일한 내용의 재제출은 idempotent no-op이다.
        store.record(result("one", 1));
        assertArrayEquals(new byte[] {1}, store.find("one").orElseThrow().request());

        // bound(1)에 도달한 상태에서 새 correlationId는 거부된다.
        assertThrows(IllegalStateException.class, () -> store.record(result("two", 3)));
    }

    @Test
    void enforcesLimitAtomicallyAcrossConcurrentWriters() throws Exception {
        CpfTcpUnknownResultStore store = new CpfTcpUnknownResultStore(1);
        CountDownLatch start = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = executor.submit(() -> recordAfter(start, store, "one"));
            var second = executor.submit(() -> recordAfter(start, store, "two"));
            start.countDown();
            int accepted = (first.get(1, TimeUnit.SECONDS) ? 1 : 0)
                    + (second.get(1, TimeUnit.SECONDS) ? 1 : 0);
            assertEquals(1, accepted);
            assertEquals(1, store.snapshot().size());
        }
    }

    @Test
    void validatesResultBeforeStorage() {
        assertThrows(IllegalArgumentException.class, () -> new CpfTcpUnknownResultStore(0));
        assertThrows(
                IllegalArgumentException.class,
                () -> new CpfTcpUnknownResult(" ", Instant.now(), new byte[] {1}, "EOF"));
    }

    private static boolean recordAfter(
            CountDownLatch start, CpfTcpUnknownResultStore store, String correlationId) throws Exception {
        start.await(1, TimeUnit.SECONDS);
        try {
            store.record(result(correlationId, 1));
            return true;
        } catch (IllegalStateException expected) {
            return false;
        }
    }

    private static CpfTcpUnknownResult result(String correlationId, int value) {
        return new CpfTcpUnknownResult(
                correlationId,
                Instant.parse("2026-08-04T00:00:00Z"),
                new byte[] {(byte) value},
                "EOF");
    }
}
