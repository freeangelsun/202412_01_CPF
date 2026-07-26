package com.cpf.core.api.retention;

import java.time.Instant;

/** Retention 실행 명령. cutoff는 ARCHIVE/PURGE 실제 실행 시 필수입니다. */
public record CpfRetentionCommand(CpfRetentionPolicy policy, Instant cutoff, String actorId, String reason) {
    public CpfRetentionCommand {
        if (policy == null) throw new IllegalArgumentException("policy는 필수입니다.");
        if (actorId == null || actorId.isBlank()) throw new IllegalArgumentException("actorId는 필수입니다.");
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("reason은 필수입니다.");
    }
}
