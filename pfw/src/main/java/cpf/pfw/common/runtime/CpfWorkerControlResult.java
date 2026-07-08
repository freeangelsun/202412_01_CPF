package cpf.pfw.common.runtime;

import java.time.Instant;

/**
 * scheduler/worker 제어 결과 후보 DTO입니다.
 */
public record CpfWorkerControlResult(
        String componentId,
        String action,
        String status,
        Instant completedAt,
        String detail) {

    public CpfWorkerControlResult {
        completedAt = completedAt == null ? Instant.now() : completedAt;
    }
}
