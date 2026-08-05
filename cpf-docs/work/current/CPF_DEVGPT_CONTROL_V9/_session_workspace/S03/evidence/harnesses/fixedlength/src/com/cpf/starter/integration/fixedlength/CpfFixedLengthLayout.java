package com.cpf.starter.integration.fixedlength;

import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Byte-oriented fixed-length layout. Offsets and lengths are bytes, not Java characters. */
public record CpfFixedLengthLayout(
        String id,
        int recordLength,
        Charset charset,
        List<CpfFixedLengthField> fields) {
    public CpfFixedLengthLayout {
        if (id == null || id.isBlank() || recordLength < 1 || charset == null || fields == null) {
            throw new IllegalArgumentException("invalid layout");
        }
        fields = List.copyOf(fields);
        boolean[] used = new boolean[recordLength];
        Set<String> names = new HashSet<>();
        for (CpfFixedLengthField field : fields) {
            if (!names.add(field.name())) {
                throw new IllegalArgumentException("duplicate field: " + field.name());
            }
            if (field.offset() + field.length() > recordLength) {
                throw new IllegalArgumentException("field exceeds record: " + field.name());
            }
            for (int i = field.offset(); i < field.offset() + field.length(); i++) {
                if (used[i]) {
                    throw new IllegalArgumentException("overlapping field: " + field.name());
                }
                used[i] = true;
            }
        }
    }
}
