package cpf.pfw.common.runtime;

import java.time.Instant;

/**
 * runtime heartbeat 기록 결과입니다.
 */
public record CpfHeartbeatResult(
        String componentId,
        String status,
        Instant recordedAt,
        String detail) {

    public CpfHeartbeatResult {
        recordedAt = recordedAt == null ? Instant.now() : recordedAt;
    }
}
