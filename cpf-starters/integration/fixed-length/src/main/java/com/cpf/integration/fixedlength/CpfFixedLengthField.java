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

    /** 고정길이 전문 Field의 좌/우 정렬 방식을 나타내는 계약 값입니다. */
    public enum Alignment {
        LEFT,
        RIGHT
    }
}
