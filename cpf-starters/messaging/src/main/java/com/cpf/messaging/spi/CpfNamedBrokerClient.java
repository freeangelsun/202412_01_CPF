package com.cpf.messaging.spi;

import com.cpf.messaging.api.CpfBrokerClient;

/** Reliability Router가 사용하는 Provider 중립 named broker binding입니다. */
public record CpfNamedBrokerClient(String name, String provider, boolean defaultBinding, CpfBrokerClient client) {
    public CpfNamedBrokerClient {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name is required");
        if (provider == null || provider.isBlank()) throw new IllegalArgumentException("provider is required");
        if (client == null) throw new IllegalArgumentException("client is required");
        name = name.trim();
        provider = provider.trim();
    }
}
