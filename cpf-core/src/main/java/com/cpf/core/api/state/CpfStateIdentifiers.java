package com.cpf.core.api.state;

import java.util.Objects;

/** Canonical validation for externally supplied state identifiers. */
public final class CpfStateIdentifiers {
    private CpfStateIdentifiers() {}

    public static String stateKey(String value) {
        return identifier(value, "stateKey", 200);
    }

    public static String operationId(String value) {
        return identifier(value, "operationId", 200);
    }

    public static String actor(String value) {
        return identifier(value, "actor", 128);
    }

    public static String identifier(String value, String field, int maximumLength) {
        String normalized = Objects.requireNonNull(value, field).trim();
        if (normalized.isEmpty() || normalized.length() > maximumLength) {
            throw new IllegalArgumentException(field + " length is invalid");
        }
        if (normalized.chars().anyMatch(Character::isISOControl)
                || !normalized.matches("[A-Za-z0-9][A-Za-z0-9._:@/-]*")) {
            throw new IllegalArgumentException(field + " contains unsupported characters");
        }
        return normalized;
    }
}
