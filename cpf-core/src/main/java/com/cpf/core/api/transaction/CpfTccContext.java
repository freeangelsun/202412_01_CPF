package com.cpf.core.api.transaction;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

/** TCC Try/Confirm/Cancel에서 동일하게 전달되는 멱등·lineage Context입니다. */
public record CpfTccContext(
        String transactionId,
        String branchId,
        String idempotencyKey,
        Instant deadline,
        Map<String, String> attributes) {
    public CpfTccContext {
        transactionId = requireText(transactionId, "transactionId");
        branchId = requireText(branchId, "branchId");
        idempotencyKey = requireText(idempotencyKey, "idempotencyKey");
        Objects.requireNonNull(deadline, "deadline");
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
        return value;
    }
}
