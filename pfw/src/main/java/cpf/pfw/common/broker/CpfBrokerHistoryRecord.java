package cpf.pfw.common.broker;

import java.time.Instant;
import java.util.Map;

/**
 * broker 송수신 이력 표준 DTO입니다.
 */
public record CpfBrokerHistoryRecord(
        String historyId,
        String direction,
        String brokerName,
        String topic,
        String messageId,
        String transactionGlobalId,
        String idempotencyKey,
        String status,
        Instant recordedAt,
        Map<String, String> attributes) {

    public CpfBrokerHistoryRecord {
        recordedAt = recordedAt == null ? Instant.now() : recordedAt;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
