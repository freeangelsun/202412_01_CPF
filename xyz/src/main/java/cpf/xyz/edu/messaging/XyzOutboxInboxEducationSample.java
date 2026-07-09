package cpf.xyz.edu.messaging;

/**
 * outbox/inbox 멱등 처리 후보를 source contract로 보여주는 샘플입니다.
 */
public class XyzOutboxInboxEducationSample {

    public OutboxRecord outbox(String aggregateId, String eventType) {
        return new OutboxRecord(aggregateId + ":" + eventType, aggregateId, eventType, "READY");
    }

    public record OutboxRecord(String idempotencyKey, String aggregateId, String eventType, String status) {
    }
}
