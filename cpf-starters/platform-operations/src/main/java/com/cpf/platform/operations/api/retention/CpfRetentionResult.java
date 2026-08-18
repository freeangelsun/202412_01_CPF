package com.cpf.platform.operations.api.retention;

/** Retention 한 chunk 실행 결과. hasMore=true이면 동일 Engine이 다음 chunk를 이어서 처리합니다. */
public record CpfRetentionResult(
        String target,
        String action,
        boolean dryRun,
        boolean legalHold,
        long matched,
        long archived,
        long purged,
        String status,
        long processed,
        boolean hasMore,
        long freedBytes) {
    public CpfRetentionResult {
        if (target == null || target.isBlank()) throw new IllegalArgumentException("target은 필수입니다.");
        if (action == null || action.isBlank()) throw new IllegalArgumentException("action은 필수입니다.");
        if (matched < 0 || archived < 0 || purged < 0 || processed < 0 || freedBytes < 0) {
            throw new IllegalArgumentException("retention count는 음수일 수 없습니다.");
        }
    }

    /** 기존 단일-chunk Consumer가 상세 실행 통계 없이 결과를 만들 때 사용하는 호환 생성자입니다. */
    public CpfRetentionResult(String target, String action, boolean dryRun, boolean legalHold,
                              long matched, long archived, long purged, String status) {
        this(target, action, dryRun, legalHold, matched, archived, purged, status,
                Math.max(archived, purged), false, 0L);
    }
}
