package com.cpf.messaging.reliability.api.jdbc;

import com.cpf.messaging.spi.CpfNamedBrokerClient;

import com.cpf.messaging.api.CpfMessagingTemplate;
import com.cpf.messaging.api.CpfBrokerPublishRequest;
import com.cpf.messaging.api.CpfBrokerPublishResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Routes the public CPF broker API to one explicitly selected named Provider. */
/** CpfMessagingTemplateRouter는 메시징 신뢰성 경계에서 중복 방지·재시도·결과불명 복구 책임을 명확히 수행합니다. */
public final class CpfMessagingTemplateRouter implements CpfMessagingTemplate {
    private final Map<String, CpfNamedBrokerClient> bindings;
    private final CpfNamedBrokerClient defaultBinding;

    /** CpfMessagingTemplateRouter 작업을 CPF 메시징 신뢰성 정책과 상태 전이 규칙에 따라 수행합니다. */
    public CpfMessagingTemplateRouter(List<CpfNamedBrokerClient> clients) {
        if (clients == null || clients.isEmpty()) {
            throw new IllegalStateException("Messaging capability requires one configured broker Provider");
        }
        Map<String, CpfNamedBrokerClient> map = new LinkedHashMap<>();
        for (CpfNamedBrokerClient client : clients) {
            if (map.putIfAbsent(client.name(), client) != null) {
                throw new IllegalStateException("Duplicate broker binding: " + client.name());
            }
        }
        List<CpfNamedBrokerClient> defaults = clients.stream()
                .filter(value -> value.defaultBinding())
                .toList();
        if (defaults.size() != 1) {
            throw new IllegalStateException(
                    "Messaging capability requires exactly one default broker binding (found="
                            + defaults.size() + ")");
        }
        bindings = Map.copyOf(map);
        defaultBinding = defaults.getFirst();
    }

    @Override
    public CpfBrokerPublishResult send(CpfBrokerPublishRequest request) {
        return defaultBinding.client().send(CpfBrokerHeaderPolicy.validatedRequest(request));
    }

    /** enqueue 작업을 CPF 메시징 신뢰성 정책과 상태 전이 규칙에 따라 수행합니다. */
    public CpfBrokerPublishResult send(String binding, CpfBrokerPublishRequest request) {
        CpfNamedBrokerClient client = bindings.get(binding);
        if (client == null) {
            throw new IllegalStateException("Unknown broker binding: " + binding);
        }
        return client.client().send(CpfBrokerHeaderPolicy.validatedRequest(request));
    }

    /** providers 작업을 CPF 메시징 신뢰성 정책과 상태 전이 규칙에 따라 수행합니다. */
    public Map<String, String> providers() {
        Map<String, String> result = new LinkedHashMap<>();
        bindings.forEach((name, client) -> result.put(name, client.provider()));
        return Map.copyOf(result);
    }
}
