package com.cpf.core.api.reliability;
import java.util.List;
import java.util.Optional;
public interface CpfEventSchemaRegistry {
    CpfEventSchemaDescriptor register(CpfEventSchemaDescriptor candidate);
    Optional<CpfEventSchemaDescriptor> latest(String subject);
    Optional<CpfEventSchemaDescriptor> byId(String schemaId);
    List<CpfEventSchemaDescriptor> history(String subject);
    CpfEventSchemaCompatibilityResult compatibility(CpfEventSchemaDescriptor previous, CpfEventSchemaDescriptor candidate);
    void validate(CpfEventSchemaDescriptor schema, byte[] payload);
}
