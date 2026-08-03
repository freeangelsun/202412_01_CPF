package com.cpf.starter.messaging.reliability;

import com.cpf.core.api.broker.CpfBrokerClient;

/** A named Provider binding hidden behind the public CPF broker client contract. */
public record CpfNamedBrokerClient(
        String name,
        String provider,
        boolean defaultBinding,
        CpfBrokerClient client) {
    public CpfNamedBrokerClient {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        if (provider == null || provider.isBlank()) {
            throw new IllegalArgumentException("provider is required");
        }
        if (client == null) {
            throw new IllegalArgumentException("client is required");
        }
        name = name.trim();
        provider = provider.trim();
    }
}
