package cpf.pfw.common.broker;

import java.time.Instant;

/**
 * DLQ 재처리 결과 DTO입니다.
 */
public record CpfBrokerDlqReplayResult(
        String messageId,
        String status,
        String replayTransactionGlobalId,
        Instant completedAt,
        String detail) {

    public CpfBrokerDlqReplayResult {
        status = status == null || status.isBlank() ? "UNKNOWN" : status;
        completedAt = completedAt == null ? Instant.now() : completedAt;
    }
}
