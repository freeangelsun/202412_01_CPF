package com.cpf.platform.operations.api.retention;

import java.time.Instant;

/** Retention 실행 명령. maxRows는 한 transaction/chunk의 상한입니다. */
public record CpfRetentionCommand(
        CpfRetentionPolicy policy,
        Instant cutoff,
        String actorId,
        String reason,
        int maxRows) {
    public CpfRetentionCommand {
        if (policy == null) throw new IllegalArgumentException("policy는 필수입니다.");
        if (actorId == null || actorId.isBlank()) throw new IllegalArgumentException("actorId는 필수입니다.");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("reason은 필수입니다.");
        if (maxRows <= 0 || maxRows > 100_000) throw new IllegalArgumentException("maxRows는 1..100000 범위여야 합니다.");
    }

    /** 기존 단건 호출 호환 경로. 실제 Execution Engine은 명시적 chunk size를 사용합니다. */
    public CpfRetentionCommand(CpfRetentionPolicy policy, Instant cutoff, String actorId, String reason) {
        this(policy, cutoff, actorId, reason, 1_000);
    }
}
