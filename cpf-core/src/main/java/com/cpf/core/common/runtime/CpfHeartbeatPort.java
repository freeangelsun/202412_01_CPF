package cpf.pfw.common.runtime;

import java.time.Instant;
import java.util.List;

/**
 * runtime heartbeat 기록/조회 port입니다.
 */
public interface CpfHeartbeatPort {

    void heartbeat(String componentId, Instant heartbeatAt);

    List<CpfRuntimeHealthStatus> findHeartbeats(String componentType);
}
