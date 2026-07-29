package com.cpf.core.api.tabular;

import java.util.List;

/** Streaming Reader의 정규화 결과와 행별 오류입니다. */
public record CpfTabularReadResult(long acceptedRows, long rejectedRows, String sha256,
                                   List<RowError> errors) {
    public CpfTabularReadResult {
        if (acceptedRows < 0 || rejectedRows < 0) throw new IllegalArgumentException("행 수는 음수일 수 없습니다.");
        sha256 = sha256 == null ? "" : sha256.trim().toLowerCase(java.util.Locale.ROOT);
        if (!sha256.isEmpty() && !sha256.matches("[0-9a-f]{64}")) {
            throw new IllegalArgumentException("sha256 형식이 올바르지 않습니다.");
        }
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
    public record RowError(long rowNumber, String column, String code, String message) {
        public RowError {
            if (rowNumber <= 0) throw new IllegalArgumentException("오류 rowNumber는 1 이상이어야 합니다.");
            column = column == null ? "" : column.trim();
            code = code == null || code.isBlank() ? "INVALID_ROW" : code.trim();
            message = message == null ? "" : message.trim();
        }
    }
}
