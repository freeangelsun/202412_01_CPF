package com.cpf.batch.context;

import com.cpf.core.api.context.CpfContextSnapshot;
import java.util.Objects;

/**
 * Core의 공통 실행 Context와 Batch Owner 메타데이터를 명시적으로 묶는 경계 객체입니다.
 * Batch 필드를 Core Snapshot의 확장 Map/Registry에 숨기지 않습니다.
 */
public record CpfBatchContextBundle(
        CpfContextSnapshot snapshot, CpfBatchContext batch, CpfCenterCutContext centerCut) {
    public CpfBatchContextBundle {
        Objects.requireNonNull(snapshot, "snapshot");
        Objects.requireNonNull(batch, "batch");
    }
    public CpfBatchContextBundle(CpfContextSnapshot snapshot, CpfBatchContext batch) { this(snapshot,batch,null); }
    public CpfBatchContextBundle withCenterCut(CpfCenterCutContext value) { return new CpfBatchContextBundle(snapshot,batch,value); }
}
