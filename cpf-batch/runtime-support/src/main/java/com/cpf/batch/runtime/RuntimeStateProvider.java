package com.cpf.batch.runtime;

import com.cpf.batch.api.ActualState;

import java.util.List;
import java.util.Map;

/**
 * BAT 실행 역할이 Control Server에 보고하는 실제 Runtime 상태입니다.
 *
 * <p>구현 누락이 정상 상태로 위장되지 않도록 모든 기본값은 fail-closed입니다. 실제 실행 역할은
 * 자신이 확인할 수 있는 readiness, capacity와 dependency 상태를 명시적으로 재정의해야 합니다.</p>
 */
public interface RuntimeStateProvider {
    default ActualState actualState() {
        return ActualState.UNKNOWN;
    }

    default boolean ready() {
        return false;
    }

    default List<String> currentExecutions() {
        return List.of();
    }

    default List<String> activeLeases() {
        return List.of();
    }

    default int availableCapacity() {
        return 0;
    }

    default long queueDepth() {
        return 0;
    }

    default boolean draining() {
        return false;
    }

    default Map<String, String> dependencyHealth() {
        return Map.of("runtimeStateProvider", "NOT_CONFIGURED");
    }

    default String lastErrorCode() {
        return "BAT_RUNTIME_STATE_PROVIDER_NOT_CONFIGURED";
    }

    default Map<String, Number> metrics() {
        return Map.of();
    }

    default long fencingToken() {
        return 0;
    }
}
