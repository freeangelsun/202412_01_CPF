package com.cpf.core.api.context;

import java.time.Instant;
import java.util.Objects;

/**
 * Async/Executor/Message/Batch Boundary 전달을 위한 불변 Core Context Snapshot입니다.
 * Owner 전용 metadata와 Transport Runtime 객체는 포함하지 않습니다.
 */
public record CpfContextSnapshot(CpfContext context, Instant capturedAt) {
    public CpfContextSnapshot {
        Objects.requireNonNull(context, "context");
        Objects.requireNonNull(capturedAt, "capturedAt");
    }

    /**
     * 별도 capture clock이 필요하지 않은 일반 경로의 deterministic snapshot입니다.
     * 실제 capture timestamp가 운영상 필요하면 {@link #capture(CpfContext, Instant)}를 사용합니다.
     */
    public static CpfContextSnapshot capture(CpfContext context) {
        Objects.requireNonNull(context, "context");
        return new CpfContextSnapshot(context, context.execution().startedAt());
    }

    /** Boundary Owner가 Clock으로 결정한 capture 시각을 포함합니다. */
    public static CpfContextSnapshot capture(CpfContext context, Instant capturedAt) {
        return new CpfContextSnapshot(context, capturedAt);
    }

    public CpfContext.CpfTransactionContext transaction() { return context.transaction(); }
    public CpfContext.CpfExecutionContext execution() { return context.execution(); }
    public CpfContext.CpfOperationContext operation() { return context.operation(); }
    /** identity 작업을 CPF 표준 계약에 따라 수행한다. */
    public CpfContext.CpfIdentityContext identity() { return context.identity(); }
    public CpfContext.CpfTenantContext tenant() { return context.tenant(); }
}
