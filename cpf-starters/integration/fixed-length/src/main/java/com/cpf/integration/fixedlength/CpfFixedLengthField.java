package com.cpf.integration.fixedlength;

import java.util.Objects;

/** Immutable byte-oriented field definition for a fixed-length record. */
public record CpfFixedLengthField(
        String name,
        int offset,
        int length,
        Alignment alignment,
        char pad,
        boolean required) {

    public CpfFixedLengthField {
        if (name == null || name.isBlank() || offset < 0 || length < 1) {
            throw new IllegalArgumentException("invalid fixed-length field");
        }
        alignment = Objects.requireNonNull(alignment, "alignment");
    }

    public enum Alignment {
        LEFT,
        RIGHT
    }
}
