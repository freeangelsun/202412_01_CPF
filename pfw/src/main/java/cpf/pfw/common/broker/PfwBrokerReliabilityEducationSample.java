package cpf.pfw.common.broker;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * PFW Broker의 outbox, inbox, DLQ, replay, idempotency 흐름을 학습하기 위한 샘플입니다.
 *
 * <p>실제 Kafka/MQ 연결은 외부 런타임 검증 영역으로 남기고, 이 샘플은 브로커 기술과 무관하게
 * 프레임워크가 반드시 유지해야 하는 메시지 계약과 장애 처리 상태 전이를 in-memory로 검증합니다.</p>
 */
public class PfwBrokerReliabilityEducationSample {

    /**
     * 한 메시지가 outbox에 저장되고, inbox 중복검사를 통과한 뒤, 실패 시 DLQ로 이동하고 replay 되는 흐름입니다.
     */
    public BrokerReliabilityScenario runReliabilityFlow(String idempotencyKey) {
        InMemoryBrokerStore store = new InMemoryBrokerStore();
        CpfBrokerEnvelope envelope = envelope("20260709030000000XYZlocal010000001", idempotencyKey);

        OutboxRecord outbox = store.saveOutbox(envelope);
        InboxRecord firstInbox = store.acceptInbox(envelope);
        InboxRecord duplicateInbox = store.acceptInbox(envelope);
        DlqRecord dlq = store.moveToDlq(envelope, "CONSUMER_TIMEOUT", "소비자 처리 제한 시간을 초과했습니다.");
        CpfBrokerDlqReplayResult replay = store.replay(dlq, "20260709030000000ADMlocal010000002");

        return new BrokerReliabilityScenario(envelope, outbox, firstInbox, duplicateInbox, dlq, replay);
    }

    /**
     * 업무 모듈은 broker SDK에 직접 의존하지 않고 PFW envelope를 먼저 만든 뒤 publish port로 넘깁니다.
     */
    public CpfBrokerEnvelope envelope(String transactionGlobalId, String idempotencyKey) {
        CpfBrokerMessage message = new CpfBrokerMessage(
                "MSG-" + idempotencyKey,
                "cpf.member.changed",
                "memberNo:M0001",
                "{\"memberNo\":\"M0001\",\"eventType\":\"CHANGED\"}".getBytes(StandardCharsets.UTF_8),
                "application/json",
                Map.of(
                        "x-cpf-transaction-global-id", transactionGlobalId,
                        "x-cpf-idempotency-key", idempotencyKey));
        return new CpfBrokerEnvelope(
                transactionGlobalId,
                "SEG-BROKER-001",
                "MBR",
                "BAT",
                idempotencyKey,
                Instant.parse("2026-07-09T03:00:00Z"),
                message,
                Map.of("retryCount", "0", "backoffMillis", "1000"));
    }

    public static final class InMemoryBrokerStore {
        private final Map<String, OutboxRecord> outbox = new LinkedHashMap<>();
        private final Map<String, InboxRecord> inbox = new LinkedHashMap<>();
        private final Map<String, DlqRecord> dlq = new LinkedHashMap<>();

        public OutboxRecord saveOutbox(CpfBrokerEnvelope envelope) {
            OutboxRecord record = new OutboxRecord(
                    envelope.message().messageId(),
                    envelope.idempotencyKey(),
                    "READY",
                    0,
                    envelope.occurredAt());
            outbox.put(record.messageId(), record);
            return record;
        }

        public InboxRecord acceptInbox(CpfBrokerEnvelope envelope) {
            String key = envelope.consumerModule() + ":" + envelope.idempotencyKey();
            InboxRecord existing = inbox.get(key);
            if (existing != null) {
                return new InboxRecord(existing.messageId(), existing.idempotencyKey(), "DUPLICATE", existing.receivedAt());
            }
            InboxRecord record = new InboxRecord(
                    envelope.message().messageId(),
                    envelope.idempotencyKey(),
                    "ACCEPTED",
                    Instant.parse("2026-07-09T03:00:01Z"));
            inbox.put(key, record);
            return record;
        }

        public DlqRecord moveToDlq(CpfBrokerEnvelope envelope, String failureCode, String failureMessage) {
            DlqRecord record = new DlqRecord(
                    envelope.message().messageId(),
                    envelope.idempotencyKey(),
                    failureCode,
                    failureMessage,
                    1,
                    Instant.parse("2026-07-09T03:00:02Z"));
            dlq.put(record.messageId(), record);
            return record;
        }

        public CpfBrokerDlqReplayResult replay(DlqRecord record, String replayTransactionGlobalId) {
            return new CpfBrokerDlqReplayResult(
                    record.messageId(),
                    "REPLAY_REQUESTED",
                    replayTransactionGlobalId,
                    Instant.parse("2026-07-09T03:00:03Z"),
                    "DLQ 메시지를 재처리 요청 상태로 전환했습니다.");
        }
    }

    public record BrokerReliabilityScenario(
            CpfBrokerEnvelope envelope,
            OutboxRecord outbox,
            InboxRecord firstInbox,
            InboxRecord duplicateInbox,
            DlqRecord dlq,
            CpfBrokerDlqReplayResult replay) {
    }

    public record OutboxRecord(
            String messageId,
            String idempotencyKey,
            String status,
            int retryCount,
            Instant createdAt) {
    }

    public record InboxRecord(
            String messageId,
            String idempotencyKey,
            String status,
            Instant receivedAt) {
    }

    public record DlqRecord(
            String messageId,
            String idempotencyKey,
            String failureCode,
            String failureMessage,
            int retryCount,
            Instant movedAt) {
    }
}
