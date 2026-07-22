package com.cpf.core.common.broker;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CpfBrokerWorkerTest {

    @Test
    void publisherWorkerStoresSuccessAndFailureResult() {
        TestOutbox outbox = new TestOutbox(List.of(envelope("OK"), envelope("FAIL")));
        DeterministicCpfBrokerPublisher publisher = new DeterministicCpfBrokerPublisher(
                "TEST_ADAPTER",
                value -> value.message().messageId().equals("FAIL"));

        CpfBrokerPublisherWorker.RunResult result = new CpfBrokerPublisherWorker(outbox, publisher)
                .runOnce("worker-1", 10);

        assertThat(result.claimedCount()).isEqualTo(2);
        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failureCount()).isEqualTo(1);
        assertThat(outbox.savedResults).extracting(CpfBrokerResult::status)
                .containsExactly("PUBLISHED", "FAILED");
    }

    @Test
    void consumerWorkerSkipsDuplicateAndMovesFailureToDlq() {
        TestInbox inbox = new TestInbox();
        TestDlq dlq = new TestDlq();
        CpfBrokerConsumerWorker worker = new CpfBrokerConsumerWorker(inbox, dlq);

        CpfBrokerConsumerWorker.ConsumeResult first = worker.consume(envelope("M1"), value -> {
            throw new IllegalStateException("업무 실패");
        });
        CpfBrokerConsumerWorker.ConsumeResult duplicate = worker.consume(
                envelope("M1"),
                value -> CpfBrokerResult.consumed("M1", "TEST", "완료"));

        assertThat(first.status()).isEqualTo("DLQ");
        assertThat(dlq.messageIds).containsExactly("M1");
        assertThat(duplicate.duplicate()).isTrue();
    }

    private CpfBrokerEnvelope envelope(String messageId) {
        return new CpfBrokerEnvelope(
                "TX-1",
                "SEG-1",
                "REF",
                "MBR",
                "IDEM-" + messageId,
                Instant.parse("2026-07-13T00:00:00Z"),
                new CpfBrokerMessage(
                        messageId,
                        "cpf.edu",
                        messageId,
                        "payload".getBytes(StandardCharsets.UTF_8),
                        "text/plain",
                        Map.of("x-cpf-transaction-global-id", "TX-1")),
                Map.of("sampleId", "REF-BROKER-RELIABILITY"));
    }

    private static final class TestOutbox implements CpfBrokerOutboxPort {
        private final List<CpfBrokerEnvelope> pending;
        private final List<CpfBrokerResult> savedResults = new ArrayList<>();

        private TestOutbox(List<CpfBrokerEnvelope> pending) {
            this.pending = new ArrayList<>(pending);
        }

        @Override
        public CpfBrokerResult saveOutbox(CpfBrokerEnvelope envelope) {
            pending.add(envelope);
            return CpfBrokerResult.accepted(envelope.message().messageId(), "TEST", null);
        }

        @Override
        public List<CpfBrokerEnvelope> claimPending(String workerId, int limit) {
            List<CpfBrokerEnvelope> claimed = List.copyOf(pending.subList(0, Math.min(limit, pending.size())));
            pending.removeAll(claimed);
            return claimed;
        }

        @Override
        public void markPublished(String messageId, CpfBrokerResult result) {
            savedResults.add(result);
        }
    }

    private static final class TestInbox implements CpfBrokerInboxPort {
        private final List<String> received = new ArrayList<>();

        @Override
        public boolean markReceived(String messageId, String idempotencyKey) {
            return !received.contains(messageId) && received.add(messageId);
        }

        @Override
        public void markConsumed(String messageId, CpfBrokerResult result) {
            // 결과 저장 여부는 worker 호출 경계만 검증합니다.
        }
    }

    private static final class TestDlq implements CpfBrokerDlqPort {
        private final List<String> messageIds = new ArrayList<>();

        @Override
        public CpfBrokerResult sendToDlq(CpfBrokerEnvelope envelope, String reason) {
            messageIds.add(envelope.message().messageId());
            return CpfBrokerResult.failed(envelope.message().messageId(), "TEST_DLQ", reason);
        }

        @Override
        public List<CpfBrokerEnvelope> findDlqMessages(String topic, int limit) {
            return List.of();
        }
    }
}
