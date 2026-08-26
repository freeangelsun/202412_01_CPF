package com.cpf.integration.fixedlength.internal;

import com.cpf.integration.fixedlength.api.*;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** byte 길이·charset·민감정보 마스킹을 지키는 기본 Fixed-Length Parser/Writer 구현입니다. */
public final class DefaultCpfFixedLengthCodec implements CpfFixedLengthParser, CpfFixedLengthWriter {
    private final CpfFixedLengthConverterRegistry converters;

    public DefaultCpfFixedLengthCodec(CpfFixedLengthConverterRegistry converters) {
        this.converters = Objects.requireNonNull(converters, "converters");
    }

    @Override
    public CpfFixedLengthParseResult parse(String message, CpfFixedLengthLayout layout) {
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(layout, "layout");
        return parse(encodeStrict(message, layout.charset()), layout);
    }

    @Override
    public CpfFixedLengthParseResult parse(byte[] message, CpfFixedLengthLayout layout) {
        Objects.requireNonNull(message, "message");
        Objects.requireNonNull(layout, "layout");
        List<CpfFixedLengthError> errors = new ArrayList<>();
        Map<String,String> fields = new LinkedHashMap<>();
        Map<String,Object> typed = new LinkedHashMap<>();
        Map<String,String> masked = new LinkedHashMap<>();
        Map<String,List<Map<String,String>>> groups = new LinkedHashMap<>();
        Map<String,List<Map<String,Object>>> typedGroups = new LinkedHashMap<>();
        Map<String,List<Map<String,String>>> maskedGroups = new LinkedHashMap<>();

        if (message.length != layout.totalLength()) {
            errors.add(new CpfFixedLengthError("_message", "CPF_FIXED_LENGTH_MISMATCH",
                    "전문 byte 길이가 Layout과 다릅니다."));
            return new CpfFixedLengthParseResult("", message.length, fields, typed, masked,
                    groups, typedGroups, maskedGroups, errors);
        }

        for (CpfFixedLengthFieldSpec spec : layout.fields()) {
            ParsedField field = readField(message, 0, spec, layout.charset(), errors, spec.name());
            fields.put(spec.name(), field.text());
            typed.put(spec.name(), field.typed());
            masked.put(spec.name(), spec.sensitive() ? mask(field.text()) : field.text());
        }

        int fallbackStart = layout.fields().stream().mapToInt(value -> value.zeroBasedEndExclusive()).max().orElse(0);
        for (CpfFixedLengthGroupSpec group : layout.groups()) {
            int count = parseCount(fields.get(group.countFieldName()), group, errors);
            int start = group.zeroBasedStart(fallbackStart);
            List<Map<String,String>> rows = new ArrayList<>();
            List<Map<String,Object>> typedRows = new ArrayList<>();
            List<Map<String,String>> maskedRows = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                int rowBase = start + (i * group.itemLength());
                Map<String,String> row = new LinkedHashMap<>();
                Map<String,Object> typedRow = new LinkedHashMap<>();
                Map<String,String> maskedRow = new LinkedHashMap<>();
                for (CpfFixedLengthFieldSpec spec : group.fields()) {
                    String path = group.name() + "[" + i + "]." + spec.name();
                    ParsedField field = readField(message, rowBase, spec, layout.charset(), errors, path);
                    row.put(spec.name(), field.text());
                    typedRow.put(spec.name(), field.typed());
                    maskedRow.put(spec.name(), spec.sensitive() ? mask(field.text()) : field.text());
                }
                rows.add(Map.copyOf(row));
                typedRows.add(Map.copyOf(typedRow));
                maskedRows.add(Map.copyOf(maskedRow));
            }
            groups.put(group.name(), List.copyOf(rows));
            typedGroups.put(group.name(), List.copyOf(typedRows));
            maskedGroups.put(group.name(), List.copyOf(maskedRows));
            fallbackStart = start + (group.itemLength() * group.maxCount());
        }

        String raw = decodeStrict(message, layout.charset());
        return new CpfFixedLengthParseResult(raw, message.length, fields, typed, masked,
                groups, typedGroups, maskedGroups, errors);
    }

    @Override
    public CpfFixedLengthWriteResult write(Map<String, ?> values, CpfFixedLengthLayout layout) {
        Objects.requireNonNull(values, "values");
        Objects.requireNonNull(layout, "layout");
        byte[] output = new byte[layout.totalLength()];
        Arrays.fill(output, singleByte(' ', layout.charset()));
        Map<String,String> masked = new LinkedHashMap<>();
        Map<String,List<Map<String,String>>> maskedGroups = new LinkedHashMap<>();

        Map<String,Object> normalized = new LinkedHashMap<>();
        values.forEach((k,v) -> normalized.put(k, v));
        for (CpfFixedLengthGroupSpec group : layout.groups()) {
            List<?> rows = asRows(normalized.get(group.name()), group.name());
            Object countValue = normalized.get(group.countFieldName());
            if (countValue == null) normalized.put(group.countFieldName(), rows.size());
            else if (Integer.parseInt(String.valueOf(countValue)) != rows.size()) {
                throw new IllegalArgumentException("반복부 count와 실제 rows 수가 다릅니다: " + group.name());
            }
            if (rows.size() > group.maxCount()) throw new IllegalArgumentException("반복부 maxCount 초과: " + group.name());
        }

        for (CpfFixedLengthFieldSpec spec : layout.fields()) {
            Object value = normalized.get(spec.name());
            String text = format(value, spec);
            writeField(output, 0, spec, text, layout.charset());
            masked.put(spec.name(), spec.sensitive() ? mask(text) : text);
        }

        int fallbackStart = layout.fields().stream().mapToInt(value -> value.zeroBasedEndExclusive()).max().orElse(0);
        for (CpfFixedLengthGroupSpec group : layout.groups()) {
            List<?> rows = asRows(normalized.get(group.name()), group.name());
            int start = group.zeroBasedStart(fallbackStart);
            List<Map<String,String>> maskedRows = new ArrayList<>();
            for (int i = 0; i < rows.size(); i++) {
                Map<?,?> row = asMap(rows.get(i), group.name() + "[" + i + "]");
                Map<String,String> maskedRow = new LinkedHashMap<>();
                int rowBase = start + (i * group.itemLength());
                for (CpfFixedLengthFieldSpec spec : group.fields()) {
                    String text = format(row.get(spec.name()), spec);
                    writeField(output, rowBase, spec, text, layout.charset());
                    maskedRow.put(spec.name(), spec.sensitive() ? mask(text) : text);
                }
                maskedRows.add(Map.copyOf(maskedRow));
            }
            maskedGroups.put(group.name(), List.copyOf(maskedRows));
            fallbackStart = start + (group.itemLength() * group.maxCount());
        }
        return new CpfFixedLengthWriteResult(decodeStrict(output, layout.charset()), output, masked, maskedGroups);
    }

    private ParsedField readField(byte[] source, int base, CpfFixedLengthFieldSpec spec, Charset charset,
                                  List<CpfFixedLengthError> errors, String path) {
        int from = base + spec.zeroBasedStart();
        int to = from + spec.length();
        String text;
        try {
            text = decodeStrict(Arrays.copyOfRange(source, from, to), charset);
            if (spec.trim()) text = trimPadding(text, spec.padding(), spec.alignment());
            if (text.isBlank() && spec.required()) throw new IllegalArgumentException("필수값 누락");
            Object typed = convert(text, spec);
            return new ParsedField(text, typed);
        } catch (RuntimeException ex) {
            errors.add(new CpfFixedLengthError(path, "CPF_FIXED_FIELD_INVALID",
                    "고정길이 필드 형식이 올바르지 않습니다.", from, from + 1, ""));
            return new ParsedField("", null);
        }
    }

    private Object convert(String text, CpfFixedLengthFieldSpec spec) {
        if (text == null || text.isBlank()) return null;
        return switch (spec.type()) {
            case STRING -> text;
            case NUMBER -> new BigInteger(text);
            case DECIMAL, AMOUNT -> spec.scale() > 0
                    ? new BigDecimal(new BigInteger(text), spec.scale())
                    : new BigDecimal(text);
            case DATE -> LocalDate.parse(text, DateTimeFormatter.BASIC_ISO_DATE);
            case TIME -> LocalTime.parse(text, DateTimeFormatter.ofPattern("HHmmss"));
            case BOOLEAN -> switch (text.trim().toUpperCase(Locale.ROOT)) {
                case "Y", "1", "TRUE" -> Boolean.TRUE;
                case "N", "0", "FALSE" -> Boolean.FALSE;
                default -> throw new IllegalArgumentException("boolean 형식 오류");
            };
            case CUSTOM -> converters.require(spec.converterId()).parse(text);
        };
    }

    private String format(Object value, CpfFixedLengthFieldSpec spec) {
        if (value == null) {
            if (!spec.defaultValue().isBlank()) return spec.defaultValue();
            if (spec.required()) throw new IllegalArgumentException("필수 fixed-length 필드가 없습니다: " + spec.name());
            return "";
        }
        return switch (spec.type()) {
            case STRING, NUMBER -> String.valueOf(value);
            case DECIMAL, AMOUNT -> {
                BigDecimal decimal = value instanceof BigDecimal b ? b : new BigDecimal(String.valueOf(value));
                yield spec.scale() > 0 ? decimal.movePointRight(spec.scale()).toBigIntegerExact().toString() : decimal.toPlainString();
            }
            case DATE -> value instanceof LocalDate d ? d.format(DateTimeFormatter.BASIC_ISO_DATE) : String.valueOf(value);
            case TIME -> value instanceof LocalTime t ? t.format(DateTimeFormatter.ofPattern("HHmmss")) : String.valueOf(value);
            case BOOLEAN -> Boolean.parseBoolean(String.valueOf(value)) || "Y".equalsIgnoreCase(String.valueOf(value)) ? "Y" : "N";
            case CUSTOM -> converters.require(spec.converterId()).write(value);
        };
    }

    private void writeField(byte[] target, int base, CpfFixedLengthFieldSpec spec, String value, Charset charset) {
        byte[] encoded = encodeStrict(value, charset);
        if (encoded.length > spec.length()) throw new IllegalArgumentException("fixed-length field byte overflow: " + spec.name());
        byte pad = singleByte(spec.padding(), charset);
        int from = base + spec.zeroBasedStart();
        Arrays.fill(target, from, from + spec.length(), pad);
        int start = spec.alignment() == CpfFixedLengthAlignment.RIGHT ? from + spec.length() - encoded.length : from;
        System.arraycopy(encoded, 0, target, start, encoded.length);
    }

    private static String trimPadding(String value, char pad, CpfFixedLengthAlignment alignment) {
        String p = String.valueOf(pad);
        if (alignment == CpfFixedLengthAlignment.RIGHT) {
            int i = 0; while (i < value.length() && value.startsWith(p, i)) i += p.length(); return value.substring(i);
        }
        int end = value.length(); while (end > 0 && value.substring(0, end).endsWith(p)) end -= p.length(); return value.substring(0, end);
    }

    private static int parseCount(String value, CpfFixedLengthGroupSpec group, List<CpfFixedLengthError> errors) {
        try {
            int count = value == null || value.isBlank() ? 0 : Integer.parseInt(value.trim());
            if (count < 0 || count > group.maxCount()) throw new IllegalArgumentException();
            return count;
        } catch (RuntimeException ex) {
            errors.add(new CpfFixedLengthError(group.countFieldName(), "CPF_FIXED_GROUP_COUNT_INVALID",
                    "반복부 count가 0~maxCount 범위를 벗어났습니다."));
            return 0;
        }
    }

    private static List<?> asRows(Object value, String group) {
        if (value == null) return List.of();
        if (value instanceof List<?> list) return list;
        throw new IllegalArgumentException("반복부 값은 List여야 합니다: " + group);
    }

    private static Map<?,?> asMap(Object value, String path) {
        if (value instanceof Map<?,?> map) return map;
        throw new IllegalArgumentException("반복부 row는 Map이어야 합니다: " + path);
    }

    private static String mask(String value) { return value == null || value.isEmpty() ? "" : "***"; }

    private static byte[] encodeStrict(String value, Charset charset) {
        try {
            ByteBuffer buffer = charset.newEncoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).encode(CharBuffer.wrap(value));
            byte[] bytes = new byte[buffer.remaining()]; buffer.get(bytes); return bytes;
        } catch (CharacterCodingException ex) { throw new IllegalArgumentException("charset encode 실패", ex); }
    }

    private static String decodeStrict(byte[] value, Charset charset) {
        try {
            return charset.newDecoder().onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT).decode(ByteBuffer.wrap(value)).toString();
        } catch (CharacterCodingException ex) { throw new IllegalArgumentException("charset decode 실패", ex); }
    }

    private static byte singleByte(char value, Charset charset) {
        byte[] encoded = encodeStrict(String.valueOf(value), charset);
        if (encoded.length != 1) throw new IllegalArgumentException("padding 문자는 정확히 1 byte여야 합니다.");
        return encoded[0];
    }

    private record ParsedField(String text, Object typed) {}
}
