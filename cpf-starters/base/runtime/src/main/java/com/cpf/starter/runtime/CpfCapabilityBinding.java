package com.cpf.starter.runtime;

import java.util.Map;
import java.util.Objects;

/** Immutable, named provider binding resolved by a versioned CPF capability profile. */
public record CpfCapabilityBinding(
        String capability,
        String name,
        String provider,
        boolean defaultBinding,
        Map<String, String> metadata) {

    public CpfCapabilityBinding {
        capability = requireText(capability, "capability");
        name = requireText(name, "name");
        provider = requireText(provider, "provider");
        metadata = Map.copyOf(Objects.requireNonNullElse(metadata, Map.of()));
    }

    private static String requireText(String value, String label) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(label + " must not be blank");
        return value.trim();
    }
}
