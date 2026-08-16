package com.cpf.platform.operations.runtimecontrol;

/** Immutable Runtime Change audit hash-chain 검증 결과입니다. */
public record CpfRuntimeAuditVerification(
        String changeId,
        boolean valid,
        long verifiedCount,
        Long firstInvalidAuditId,
        String expectedHash,
        String actualHash,
        String message) {
}
