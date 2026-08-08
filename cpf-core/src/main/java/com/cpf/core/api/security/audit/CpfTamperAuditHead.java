package com.cpf.core.api.security.audit;

/** Append-only 감사 원장의 현재 Sequence와 Hash Head를 나타냅니다. */
public record CpfTamperAuditHead(long sequence, String currentHash) {
    public CpfTamperAuditHead {
        if (sequence < 0) throw new IllegalArgumentException("sequence must be >= 0");
        if (currentHash == null || currentHash.isBlank()) throw new IllegalArgumentException("currentHash is required");
    }
}
