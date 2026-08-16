package com.cpf.platform.operations.api.state;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** Bounded cursor query for operational state projections. */
/** CpfStateSearchRequest 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfStateSearchRequest(
        String stateKeyPrefix,
        Set<CpfOperationState> states,
        String afterStateKey,
        int pageSize) {

    public CpfStateSearchRequest {
        stateKeyPrefix = normalizePrefix(stateKeyPrefix);
        states = states == null || states.isEmpty()
                ? Collections.unmodifiableSet(EnumSet.allOf(CpfOperationState.class))
                : Collections.unmodifiableSet(EnumSet.copyOf(states));
        afterStateKey = afterStateKey == null || afterStateKey.isBlank()
                ? null : CpfStateIdentifiers.stateKey(afterStateKey);
        if (pageSize < 1 || pageSize > 200) {
            throw new IllegalArgumentException("pageSize must be between 1 and 200");
        }
    }

    /** firstPage 작업을 CPF 표준 계약에 따라 수행한다. */
    public static CpfStateSearchRequest firstPage(int pageSize) {
        return new CpfStateSearchRequest("", Set.of(), null, pageSize);
    }

    private static String normalizePrefix(String value) {
        if (value == null || value.isBlank()) return "";
        String normalized = value.trim();
        if (normalized.length() > 200 || normalized.chars().anyMatch(Character::isISOControl)
                || !normalized.matches("[A-Za-z0-9][A-Za-z0-9._:@/-]*")) {
            throw new IllegalArgumentException("stateKeyPrefix contains unsupported characters");
        }
        return normalized;
    }
}
