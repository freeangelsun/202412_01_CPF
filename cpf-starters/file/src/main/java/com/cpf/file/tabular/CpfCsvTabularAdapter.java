package com.cpf.file.tabular;

import com.cpf.file.tabular.api.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/** RFC4180 기반 CSV Streaming Reader/Writer입니다. */
public final class CpfCsvTabularAdapter implements CpfTabularReader, CpfTabularWriter {
    private static final int MAX_REPORTED_ERRORS = 10_000;

    @Override

    public boolean supports(CpfTabularFormat format) { return format == CpfTabularFormat.CSV; }

    @Override
    public CpfTabularReadResult read(CpfTabularReadRequest request, CpfTabularRowConsumer consumer) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(consumer, "consumer");
        if (!supports(request.format())) throw new IllegalArgumentException("CSV 요청이 아닙니다.");
        List<CpfTabularReadResult.RowError> errors = new ArrayList<>();
        long accepted = 0, rejected = 0;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new DigestInputStream(request.input(), digest), StandardCharsets.UTF_8))) {
                List<String> header = readRecord(reader, request.schema().maxCellLength(),
                        request.schema().columns().size() + 1);
                validateHeader(header, request.schema());
                long rowNumber = 1;
                List<String> rawValues;
                while ((rawValues = readRecord(reader, request.schema().maxCellLength(),
                        request.schema().columns().size() + 1)) != null) {
                    rowNumber++;
                    if (rowNumber - 1 > request.schema().maxRows()) {
                        throw new IllegalArgumentException("허용 행 수를 초과했습니다: " + request.schema().maxRows());
                    }
                    Map<String,String> row = new LinkedHashMap<>();
                    boolean valid = true;
                    for (int index = 0; index < request.schema().columns().size(); index++) {
                        CpfTabularColumn column = request.schema().columns().get(index);
                        String value = index < rawValues.size() ? normalize(rawValues.get(index), column.type()) : "";
                        String code = validateValue(column, value, request.rejectFormula(), false);
                        if (code != null) {
                            valid = false;
                            addError(errors, rowNumber, column.name(), code,
                                    "행 " + rowNumber + "의 " + column.label() + " 값이 올바르지 않습니다.");
                        }
                        row.put(column.name(), value);
                    }
                    if (rawValues.size() > request.schema().columns().size()) {
                        valid = false;
                        addError(errors, rowNumber, "", "EXTRA_COLUMN", "정의되지 않은 Column이 존재합니다.");
                    }
                    if (valid) {
                        consumer.accept(new CpfTabularRow(rowNumber, row));
                        accepted++;
                    } else rejected++;
                }
            }
            return new CpfTabularReadResult(accepted, rejected, hex(digest.digest()), errors);
        } catch (Exception error) {
            throw error instanceof RuntimeException runtime ? runtime
                    : new IllegalStateException("CSV Streaming 처리에 실패했습니다.", error);
        }
    }

    @Override
    public String write(CpfTabularWriteRequest request) {
        Objects.requireNonNull(request, "request");
        if (!supports(request.format())) throw new IllegalArgumentException("CSV 요청이 아닙니다.");
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (java.security.DigestOutputStream digestOut =
                         new java.security.DigestOutputStream(request.output(), digest);
                 BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(digestOut, StandardCharsets.UTF_8));
                 var rows = request.rows()) {
                writer.write("\uFEFF");
                writeRecord(writer, request.schema().columns().stream().map(CpfTabularColumn::label).toList());
                Set<String> allowed = new HashSet<>();
                request.schema().columns().forEach(column -> allowed.add(column.name()));
                AtomicLong count = new AtomicLong();
                rows.forEachOrdered(row -> {
                    Objects.requireNonNull(row, "row");
                    long current = count.incrementAndGet();
                    if (current > request.schema().maxRows()) throw new IllegalArgumentException("허용 행 수를 초과했습니다.");
                    Set<String> unknown = new HashSet<>(row.values().keySet());
                    unknown.removeAll(allowed);
                    if (!unknown.isEmpty()) throw new IllegalArgumentException("정의되지 않은 Column: " + unknown);
                    try {
                        List<String> values = new ArrayList<>(request.schema().columns().size());
                        for (CpfTabularColumn column : request.schema().columns()) {
                            String value = normalize(row.values().getOrDefault(column.name(), ""), column.type());
                            String code = validateValue(column, value, false, false);
                            if (code != null) throw new IllegalArgumentException(column.name() + " 출력값 오류: " + code);
                            if (isFormula(value) && !request.escapeFormula()) {
                                throw new IllegalArgumentException(column.name() + "에 CSV Formula Injection 위험값이 있습니다.");
                            }
                            values.add(sanitize(value, request.escapeFormula()));
                        }
                        writeRecord(writer, values);
                    } catch (IOException io) { throw new UncheckedIOException(io); }
                });
                writer.flush();
            }
            return hex(digest.digest());
        } catch (Exception error) {
            throw error instanceof RuntimeException runtime ? runtime
                    : new IllegalStateException("CSV Streaming 생성에 실패했습니다.", error);
        }
    }

    private void validateHeader(List<String> actual, CpfTabularSchema schema) {
        if (actual == null) throw new IllegalArgumentException("CSV Header가 없습니다.");
        if (!actual.isEmpty()) actual.set(0, stripBom(actual.getFirst()));
        List<String> expected = schema.columns().stream().map(CpfTabularColumn::label).toList();
        if (!expected.equals(actual)) throw new IllegalArgumentException("CSV Header가 Template version과 일치하지 않습니다.");
    }

    static String validateValue(CpfTabularColumn column, String value, boolean rejectFormula, boolean formulaCell) {
        if (column.required() && value.isBlank()) return "REQUIRED";
        int limit = column.maxLength() > 0 ? column.maxLength() : Integer.MAX_VALUE;
        if (value.length() > limit) return "MAX_LENGTH";
        if (rejectFormula && (formulaCell || isFormula(value))) return "FORMULA";
        if (value.isBlank()) return null;
        try {
            switch (column.type()) {
                case INTEGER -> Long.parseLong(value);
                case DECIMAL -> new java.math.BigDecimal(value);
                case BOOLEAN -> {
                    if (!Set.of("Y","N","TRUE","FALSE","1","0").contains(value.toUpperCase(Locale.ROOT))) return "TYPE";
                }
                case DATE -> java.time.LocalDate.parse(value);
                case DATETIME -> java.time.OffsetDateTime.parse(value);
                default -> { }
            }
        } catch (RuntimeException invalid) { return "TYPE"; }
        return null;
    }

    static boolean isFormula(String value) {
        if (value == null || value.isEmpty()) return false;
        char first = value.charAt(0);
        return first == '=' || first == '+' || first == '-' || first == '@';
    }

    static String sanitize(String value, boolean escape) {
        String normalized = value == null ? "" : value.replace("\0", "");
        return escape && isFormula(normalized) ? "'" + normalized : normalized;
    }

    static String normalize(String value, CpfTabularColumn.Type type) {
        String normalized = value == null ? "" : value.replace("\0", "");
        return type == CpfTabularColumn.Type.STRING ? normalized : normalized.trim();
    }

    static void addError(List<CpfTabularReadResult.RowError> errors, long rowNumber,
                         String column, String code, String message) {
        if (errors.size() < MAX_REPORTED_ERRORS - 1) {
            errors.add(new CpfTabularReadResult.RowError(rowNumber, column, code, message));
        } else if (errors.size() == MAX_REPORTED_ERRORS - 1) {
            errors.add(new CpfTabularReadResult.RowError(rowNumber, "", "ERROR_LIMIT_REACHED",
                    "행별 오류 상세가 " + MAX_REPORTED_ERRORS + "건으로 제한되었습니다."));
        }
    }

    private String stripBom(String value) {
        return value != null && !value.isEmpty() && value.charAt(0) == '\uFEFF' ? value.substring(1) : value;
    }

    private List<String> readRecord(BufferedReader reader, int maxCellLength, int maxColumns) throws IOException {
        StringBuilder field = new StringBuilder();
        List<String> values = new ArrayList<>();
        boolean quoted = false, quoteClosed = false, any = false;
        int current;
        while ((current = reader.read()) != -1) {
            any = true;
            char c = (char) current;
            if (quoted) {
                if (c == '"') {
                    reader.mark(1);
                    int next = reader.read();
                    if (next == '"') field.append('"');
                    else {
                        quoted = false;
                        quoteClosed = true;
                        if (next != -1) reader.reset();
                    }
                } else field.append(c);
            } else if (quoteClosed) {
                if (c == ',') {
                    addField(values, field, maxColumns);
                    quoteClosed = false;
                } else if (c == '\n') {
                    addField(values, field, maxColumns);
                    return values;
                } else if (c == '\r') {
                    reader.mark(1);
                    int next = reader.read();
                    if (next != '\n' && next != -1) reader.reset();
                    addField(values, field, maxColumns);
                    return values;
                } else {
                    throw new IllegalArgumentException("CSV 닫는 따옴표 뒤에는 구분자 또는 줄바꿈만 허용합니다.");
                }
            } else if (c == '"' && field.isEmpty()) quoted = true;
            else if (c == '"') throw new IllegalArgumentException("CSV 따옴표 위치가 올바르지 않습니다.");
            else if (c == ',') addField(values, field, maxColumns);
            else if (c == '\n') { addField(values, trimCr(field), maxColumns); return values; }
            else field.append(c);
            if (field.length() > maxCellLength) throw new IllegalArgumentException("CSV Cell 길이를 초과했습니다.");
        }
        if (!any) return null;
        if (quoted) throw new IllegalArgumentException("CSV 인용부호가 닫히지 않았습니다.");
        addField(values, trimCr(field), maxColumns);
        return values;
    }

    private void addField(List<String> values, StringBuilder field, int maxColumns) {
        addField(values, field.toString(), maxColumns);
        field.setLength(0);
    }

    private void addField(List<String> values, String value, int maxColumns) {
        values.add(value);
        if (values.size() > maxColumns) throw new IllegalArgumentException("CSV Column 수가 허용 범위를 초과했습니다.");
    }

    private String trimCr(StringBuilder field) {
        int length = field.length();
        if (length > 0 && field.charAt(length - 1) == '\r') field.setLength(length - 1);
        return field.toString();
    }

    private void writeRecord(BufferedWriter writer, List<String> values) throws IOException {
        for (int index=0; index<values.size(); index++) {
            if (index>0) writer.write(',');
            String value = values.get(index) == null ? "" : values.get(index);
            boolean quote = value.indexOf(',')>=0 || value.indexOf('"')>=0 || value.indexOf('\n')>=0 || value.indexOf('\r')>=0;
            if (quote) writer.write('"');
            writer.write(value.replace("\"", "\"\""));
            if (quote) writer.write('"');
        }
        writer.newLine();
    }

    static String hex(byte[] digest) { return java.util.HexFormat.of().formatHex(digest); }
}
