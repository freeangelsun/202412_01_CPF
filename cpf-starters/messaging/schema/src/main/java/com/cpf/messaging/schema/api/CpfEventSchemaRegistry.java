package com.cpf.messaging.schema.api;

import java.util.List;
import java.util.Optional;

/** Producer/Consumer가 공유하는 이벤트 스키마 등록·조회·호환성·Runtime 검증 계약. */
public interface CpfEventSchemaRegistry {
    CpfEventSchemaDescriptor register(CpfEventSchemaDescriptor candidate);
    Optional<CpfEventSchemaDescriptor> latest(String subject);
    Optional<CpfEventSchemaDescriptor> byId(String schemaId);
    List<CpfEventSchemaDescriptor> history(String subject);
    CpfEventSchemaCompatibilityResult compatibility(CpfEventSchemaDescriptor previous, CpfEventSchemaDescriptor candidate);
    void validate(CpfEventSchemaDescriptor schema, byte[] payload);
}
