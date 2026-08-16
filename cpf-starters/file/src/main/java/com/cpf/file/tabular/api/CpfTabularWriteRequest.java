package com.cpf.file.tabular.api;

import java.io.OutputStream;
import java.util.Objects;
import java.util.stream.Stream;

/** SXSSF/CSV Streaming Write 요청입니다. */
public record CpfTabularWriteRequest(CpfTabularFormat format, CpfTabularSchema schema,
                                     Stream<CpfTabularRow> rows, OutputStream output,
                                     boolean escapeFormula) {
    public CpfTabularWriteRequest {
        format = Objects.requireNonNull(format, "format은 필수입니다.");
        schema = Objects.requireNonNull(schema, "schema는 필수입니다.");
        rows = Objects.requireNonNull(rows, "rows는 필수입니다.");
        output = Objects.requireNonNull(output, "output은 필수입니다.");
    }
}
