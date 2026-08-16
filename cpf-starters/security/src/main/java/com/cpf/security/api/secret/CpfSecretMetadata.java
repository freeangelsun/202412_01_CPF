package com.cpf.security.api.secret;

import java.time.Instant;
import java.util.Map;

/** Secret 원문을 포함하지 않는 운영용 Metadata. */
public record CpfSecretMetadata(
        CpfSecretReference reference,
        String version,
        Instant createdAt,
        Instant expiresAt,
        boolean rotatable,
        Map<String,String> attributes) {
    public CpfSecretMetadata {
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
