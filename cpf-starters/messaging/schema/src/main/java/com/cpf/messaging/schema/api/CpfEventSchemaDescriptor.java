package com.cpf.messaging.schema.api;

import java.util.Map;
import java.util.Objects;

/** 이벤트 스키마의 불변 버전 Descriptor. */
public record CpfEventSchemaDescriptor(
        String subject,
        int version,
        String schemaId,
        CpfEventSchemaFormat format,
        String contentType,
        String canonicalSchema,
        CpfEventSchemaCompatibility compatibility,
        Map<String, String> metadata) {
    public CpfEventSchemaDescriptor {
        subject = requireText(subject, "subject");
        if (version < 1) throw new IllegalArgumentException("version must be >= 1");
        schemaId = requireText(schemaId, "schemaId");
        format = Objects.requireNonNull(format, "format");
        contentType = requireText(contentType, "contentType");
        canonicalSchema = requireText(canonicalSchema, "canonicalSchema");
        compatibility = Objects.requireNonNull(compatibility, "compatibility");
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    private static String requireText(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " required");
        return value;
    }
}
