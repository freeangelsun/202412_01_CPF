package com.cpf.core.common.runtime;

import java.time.Instant;
import java.util.Map;

/**
 * runtime heartbeat 기록 요청입니다.
 */
public record CpfHeartbeatRequest(
        String componentId,
        String componentType,
        Instant heartbeatAt,
        Map<String, String> attributes) {

    public CpfHeartbeatRequest {
        heartbeatAt = heartbeatAt == null ? Instant.now() : heartbeatAt;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
