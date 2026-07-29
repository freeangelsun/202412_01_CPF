package com.cpf.core.api.tabular;

import java.io.InputStream;
import java.util.Objects;

/** 전체 파일을 메모리에 올리지 않는 Streaming Read 요청입니다. */
public record CpfTabularReadRequest(CpfTabularFormat format, CpfTabularSchema schema,
                                    InputStream input, boolean rejectFormula, boolean rejectMacro) {
    public CpfTabularReadRequest {
        format = Objects.requireNonNull(format, "format은 필수입니다.");
        schema = Objects.requireNonNull(schema, "schema는 필수입니다.");
        input = Objects.requireNonNull(input, "input은 필수입니다.");
    }
}
