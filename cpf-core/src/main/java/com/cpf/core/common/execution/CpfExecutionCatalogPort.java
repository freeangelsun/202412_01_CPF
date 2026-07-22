package com.cpf.core.common.execution;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** 표준 실행 catalog 등록·조회 port입니다. */
public interface CpfExecutionCatalogPort {
    void upsertAll(Collection<CpfExecutionDefinition> definitions);

    List<CpfExecutionDefinition> findAll();

    Optional<CpfExecutionDefinition> findById(String standardExecutionId);

    /** 신규 ID 또는 구형 alias를 현재 실행 정의로 해석합니다. */
    default Optional<CpfExecutionDefinition> resolve(String executionId) {
        return findById(executionId);
    }
}
