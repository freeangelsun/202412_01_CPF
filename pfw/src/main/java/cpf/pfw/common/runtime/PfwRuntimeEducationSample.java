package cpf.pfw.common.runtime;

import java.time.Instant;
import java.util.Map;

/**
 * ADM 관제에서 조회할 runtime 상태를 source contract 수준으로 고정하는 샘플입니다.
 */
public class PfwRuntimeEducationSample {

    public RuntimeStatus heartbeat(String moduleId, String instanceId) {
        return new RuntimeStatus(
                moduleId,
                instanceId,
                "UP",
                Instant.parse("2026-07-08T00:00:00Z"),
                Map.of("ghostDetection", "enabled", "distributedLock", "candidate"));
    }

    public record RuntimeStatus(
            String moduleId,
            String instanceId,
            String status,
            Instant heartbeatAt,
            Map<String, String> capabilities) {
    }
}
