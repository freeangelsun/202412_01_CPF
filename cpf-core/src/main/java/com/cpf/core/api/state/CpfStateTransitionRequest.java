package com.cpf.core.api.state;

import com.cpf.core.api.security.CpfSensitiveData;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/** Compare-and-set transition command with a deterministic idempotency fingerprint. */
public record CpfStateTransitionRequest(
        String stateKey,
        long expectedVersion,
        CpfOperationState targetState,
        String operationId,
        String actor,
        String reason) {

    public CpfStateTransitionRequest {
        stateKey = CpfStateIdentifiers.stateKey(stateKey);
        if (expectedVersion < -1L) {
            throw new IllegalArgumentException("expectedVersion must be -1 or non-negative");
        }
        targetState = Objects.requireNonNull(targetState, "targetState");
        operationId = CpfStateIdentifiers.operationId(operationId);
        actor = CpfStateIdentifiers.actor(actor);
        reason = CpfSensitiveData.sanitizeAuditReason(reason);
    }

    /** Stable command hash used to distinguish a replay from operation-id scope reuse. */
    public String commandHash() {
        String canonical = stateKey + '\n' + expectedVersion + '\n' + targetState.name()
                + '\n' + operationId + '\n' + actor + '\n' + reason;
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }
}
