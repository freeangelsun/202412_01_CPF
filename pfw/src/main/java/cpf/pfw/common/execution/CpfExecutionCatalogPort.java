package cpf.pfw.common.execution;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** 표준 실행 catalog 등록·조회 port입니다. */
public interface CpfExecutionCatalogPort {
    void upsertAll(Collection<CpfExecutionDefinition> definitions);

    List<CpfExecutionDefinition> findAll();

    Optional<CpfExecutionDefinition> findById(String standardExecutionId);
}
