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
        String httpMethod,
        String endpoint,
        String operationId,
        String description,
        String requiredPermission,
        boolean auditReasonRequired,
        String visibility,
        boolean directAllowed,
        boolean gatewayAllowed,
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
        httpMethod = httpMethod == null ? "" : httpMethod;
        description = description == null ? "" : description;
        requiredPermission = requiredPermission == null ? "" : requiredPermission;
        visibility = visibility == null || visibility.isBlank() ? "INTERNAL" : visibility.trim().toUpperCase();
        discoveredAt = discoveredAt == null ? Instant.now() : discoveredAt;
    }

    /** 기존 저장소·테스트에서 사용하는 최소 생성자 호환 경계입니다. */
    public CpfExecutionDefinition(
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
        this(standardExecutionId, executionName, executionType, ownerDomain, sourceModule,
                sourceClass, sourceMethod, "", endpoint, operationId, "", "", false,
                executionType == CpfExecutionType.ONLINE ? "PUBLIC" : "INTERNAL",
                true, executionType == CpfExecutionType.ONLINE, sourceVersion, discoveredAt);
    }
}
