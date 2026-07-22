package cpf.pfw.common.runtime;

import java.time.Instant;

/**
 * 장애 또는 비정상 종료가 의심되는 runtime 후보입니다.
 */
public record CpfRuntimeGhostCandidate(
        String componentId,
        String componentType,
        Instant lastHeartbeatAt,
        String reason) {
}
