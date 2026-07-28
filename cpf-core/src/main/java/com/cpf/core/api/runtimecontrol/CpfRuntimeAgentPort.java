package com.cpf.core.api.runtimecontrol;

import java.time.Instant;
import java.util.List;

/** Runtime Agent가 Control Plane에 접근하는 topology-independent SPI입니다. */
public interface CpfRuntimeAgentPort {
    CpfRuntimeInstanceLease register(CpfRuntimeInstanceRegistration registration);

    CpfRuntimeInstanceLease heartbeat(String instanceId, long fencingToken, String actualHash, long actualVersion);

    default CpfRuntimeInstanceLease heartbeat(String instanceId, long fencingToken, String actualHash,
                                               long actualVersion, Instant agentTime) {
        return heartbeat(instanceId, fencingToken, actualHash, actualVersion);
    }

    List<CpfRuntimeDelivery> claim(String instanceId, long fencingToken, int limit);

    CpfRuntimeChangeResult acknowledge(CpfRuntimeAck ack);

    /** 종료 시 lease를 즉시 해제해 stale routing 시간을 줄입니다. */
    default void deregister(String instanceId, long fencingToken, String reason) {
        // 구버전 Adapter 호환을 위한 기본 no-op입니다. 제품 Adapter는 반드시 override합니다.
    }

    /** DB Restore/Agent 재기동 후 durable Inbox의 기능별 실제 상태를 다시 증명합니다. */
    default void reconcileActualState(String instanceId, long fencingToken, List<CpfRuntimeActualState> states) {
        // 구버전 Adapter 호환을 위한 기본 no-op입니다. 제품 Adapter는 반드시 override합니다.
    }
}
