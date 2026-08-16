package com.cpf.integration.fixedlength;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/** Strict byte-oriented fixed-length encoder/decoder. */
public final class CpfFixedLengthCodec {
    private final CpfFixedLengthLayout layout;
    private final byte defaultPad;
    private final Map<String, CpfFixedLengthField> fieldsByName;

    public CpfFixedLengthCodec(CpfFixedLengthLayout layout) {
        this.layout = Objects.requireNonNull(layout, "layout");
        this.defaultPad = singleByte(' ', "record default pad");
        this.fieldsByName = layout.fields().stream().collect(Collectors.toUnmodifiableMap(
                CpfFixedLengthField::name,
                field -> field));
        for (CpfFixedLengthField field : layout.fields()) {
            singleByte(field.pad(), "field pad: " + field.name());
        }
    }

    public byte[] encode(Map<String, String> values) {
        Objects.requireNonNull(values, "values");
        Set<String> unknownFields = values.keySet().stream()
                .filter(name -> !fieldsByName.containsKey(name))
                .collect(Collectors.toUnmodifiableSet());
        if (!unknownFields.isEmpty()) {
            throw new IllegalArgumentException("unknown fields: " + unknownFields);
        }
        byte[] record = new byte[layout.recordLength()];
        Arrays.fill(record, defaultPad);
        for (CpfFixedLengthField field : layout.fields()) {
            String value = values.get(field.name());
            if ((value == null || value.isEmpty()) && field.required()) {
                throw new IllegalArgumentException("required field: " + field.name());
            }
            byte[] encoded = encodeStrict(value == null ? "" : value);
            if (encoded.length > field.length()) {
                throw new IllegalArgumentException("field byte overflow: " + field.name());
            }
            byte pad = singleByte(field.pad(), "field pad: " + field.name());
            Arrays.fill(record, field.offset(), field.offset() + field.length(), pad);
            int start = field.alignment() == CpfFixedLengthField.Alignment.LEFT
                    ? field.offset()
                    : field.offset() + field.length() - encoded.length;
            System.arraycopy(encoded, 0, record, start, encoded.length);
        }
        return record;
    }

    public Map<String, String> decode(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes");
        if (bytes.length != layout.recordLength()) {
            throw new IllegalArgumentException("record byte length mismatch");
        }
        Map<String, String> result = new LinkedHashMap<>();
        for (CpfFixedLengthField field : layout.fields()) {
            byte pad = singleByte(field.pad(), "field pad: " + field.name());
            int start = field.offset();
            int end = field.offset() + field.length();
            if (field.alignment() == CpfFixedLengthField.Alignment.RIGHT) {
                while (start < end && bytes[start] == pad) {
                    start++;
                }
            } else {
                while (end > start && bytes[end - 1] == pad) {
                    end--;
                }
            }
            result.put(field.name(), decodeStrict(Arrays.copyOfRange(bytes, start, end)));
        }
        return Map.copyOf(result);
    }

    private byte[] encodeStrict(String value) {
        try {
            ByteBuffer buffer = strictEncoder().encode(CharBuffer.wrap(value));
            byte[] output = new byte[buffer.remaining()];
            buffer.get(output);
            return output;
        } catch (CharacterCodingException ex) {
            throw new IllegalArgumentException("record contains unmappable characters", ex);
        }
    }

    private String decodeStrict(byte[] value) {
        try {
            return strictDecoder().decode(ByteBuffer.wrap(value)).toString();
        } catch (CharacterCodingException ex) {
            throw new IllegalArgumentException("record contains malformed or truncated bytes", ex);
        }
    }

    private byte singleByte(char value, String field) {
        byte[] encoded = encodeStrict(String.valueOf(value));
        if (encoded.length != 1) {
            throw new IllegalArgumentException(field + " must encode to exactly one byte");
        }
        return encoded[0];
    }

    private CharsetEncoder strictEncoder() {
        return layout.charset().newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
    }

    private CharsetDecoder strictDecoder() {
        return layout.charset().newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
    }
}
