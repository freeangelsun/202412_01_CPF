package cpf.pfw.common.broker;

import java.time.Instant;

/**
 * broker publish/consume 처리 결과입니다.
 */
public record CpfBrokerResult(
        String status,
        String messageId,
        String brokerName,
        String partitionKey,
        Instant processedAt,
        String detail) {

    public CpfBrokerResult {
        status = status == null || status.isBlank() ? "UNKNOWN" : status;
        processedAt = processedAt == null ? Instant.now() : processedAt;
    }

    public static CpfBrokerResult accepted(String messageId, String brokerName, String partitionKey) {
        return new CpfBrokerResult("ACCEPTED", messageId, brokerName, partitionKey, Instant.now(), null);
    }

    public static CpfBrokerResult failed(String messageId, String brokerName, String detail) {
        return new CpfBrokerResult("FAILED", messageId, brokerName, null, Instant.now(), detail);
    }
}
