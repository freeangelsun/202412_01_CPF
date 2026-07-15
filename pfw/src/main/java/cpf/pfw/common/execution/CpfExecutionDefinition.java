package cpf.pfw.common.execution;

import java.time.Instant;

/** 표준 실행 ID catalog에 저장하는 source binding 정보입니다. */
public record CpfExecutionDefinition(
        String standardExecutionId,
        String executionName,
        CpfExecutionType executionType,
        String ownerDomain,
        String sourceModule,
        String sourceClass,
        String sourceMethod,
        String endpoint,
        String operationId,
        String sourceVersion,
        Instant discoveredAt) {

    public CpfExecutionDefinition {
        CpfStandardExecutionId parsed = CpfStandardExecutionId.parse(standardExecutionId);
        if (parsed.type() != executionType) {
            throw new IllegalArgumentException("표준 실행 ID 유형과 catalog 실행 유형이 일치하지 않습니다.");
        }
        if (executionName == null || executionName.isBlank()) {
            throw new IllegalArgumentException("표준 실행명은 필수입니다.");
        }
        ownerDomain = ownerDomain == null || ownerDomain.isBlank() ? parsed.domain() : ownerDomain;
        discoveredAt = discoveredAt == null ? Instant.now() : discoveredAt;
    }
}
