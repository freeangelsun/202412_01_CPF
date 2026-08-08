package com.cpf.core.api.reliability;

import java.util.Map;

public record CpfEventSchemaDescriptor(
        String subject,
        int version,
        String schemaId,
        CpfEventSchemaFormat format,
        String contentType,
        String canonicalSchema,
        CpfEventSchemaCompatibility compatibility,
        Map<String, String> metadata) {
    public CpfEventSchemaDescriptor { metadata = metadata == null ? Map.of() : Map.copyOf(metadata); }
}
