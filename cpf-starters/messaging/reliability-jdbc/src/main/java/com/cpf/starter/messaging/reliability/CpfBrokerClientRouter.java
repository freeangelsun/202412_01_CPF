package com.cpf.starter.messaging.reliability;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Routes the public CPF broker API to one explicitly selected named Provider. */
public final class CpfBrokerClientRouter implements CpfBrokerClient {
    private final Map<String, CpfNamedBrokerClient> bindings;
    private final CpfNamedBrokerClient defaultBinding;

    public CpfBrokerClientRouter(List<CpfNamedBrokerClient> clients) {
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
                .filter(CpfNamedBrokerClient::defaultBinding)
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
    public CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest request) {
        return defaultBinding.client().enqueue(request);
    }

    public CpfBrokerPublishResult enqueue(String binding, CpfBrokerPublishRequest request) {
        CpfNamedBrokerClient client = bindings.get(binding);
        if (client == null) {
            throw new IllegalStateException("Unknown broker binding: " + binding);
        }
        return client.client().enqueue(request);
    }

    public Map<String, String> providers() {
        Map<String, String> result = new LinkedHashMap<>();
        bindings.forEach((name, client) -> result.put(name, client.provider()));
        return Map.copyOf(result);
    }
}
