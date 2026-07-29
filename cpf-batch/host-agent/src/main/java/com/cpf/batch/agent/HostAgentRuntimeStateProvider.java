package com.cpf.batch.agent;

import com.cpf.batch.api.ActualState;
import com.cpf.batch.runtime.RuntimeStateProvider;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Host Agent의 승인 서비스 카탈로그 구성 상태를 Runtime readiness로 보고합니다.
 */
@Component
public final class HostAgentRuntimeStateProvider implements RuntimeStateProvider {
    private final AgentProperties properties;

    public HostAgentRuntimeStateProvider(AgentProperties properties) {
        this.properties = properties;
    }

    @Override
    public ActualState actualState() {
        return ready() ? ActualState.READY : ActualState.DEGRADED;
    }

    @Override
    public boolean ready() {
        return properties.getServices() != null
                && !properties.getServices().isEmpty()
                && properties.getServices().values().stream()
                .allMatch(service -> service.getServiceId() != null
                        && !service.getServiceId().isBlank());
    }

    @Override
    public int availableCapacity() {
        return ready() ? properties.getServices().size() : 0;
    }

    @Override
    public Map<String, String> dependencyHealth() {
        return Map.of("approvedServiceCatalog", ready() ? "UP" : "NOT_CONFIGURED");
    }

    @Override
    public String lastErrorCode() {
        return ready() ? null : "BAT_AGENT_SERVICE_CATALOG_NOT_CONFIGURED";
    }
}
