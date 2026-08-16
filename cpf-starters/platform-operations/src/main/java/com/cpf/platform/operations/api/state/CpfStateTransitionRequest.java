package com.cpf.platform.operations.api.state;

import com.cpf.security.api.CpfSensitiveData;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/** Compare-and-set transition command with a deterministic idempotency fingerprint. */
/** CpfStateTransitionRequest 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
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
    /** commandHash 작업을 CPF 표준 계약에 따라 수행한다. */
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
