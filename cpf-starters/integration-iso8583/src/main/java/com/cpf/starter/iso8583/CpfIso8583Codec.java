package com.cpf.starter.iso8583;

import java.nio.charset.Charset;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class CpfIso8583Codec {
    private final Map<Integer, CpfIso8583FieldSpec> specs;
    private final Charset charset;

    public CpfIso8583Codec(Map<Integer, CpfIso8583FieldSpec> specs, Charset charset) {
        this.specs = Map.copyOf(specs);
        this.charset = charset;
    }

    public byte[] encode(CpfIso8583Message message) {
        BitSet fields = new BitSet(128);
        message.fields().keySet().forEach(number -> {
            if (number < 2 || number > 128) throw new IllegalArgumentException("invalid ISO8583 field " + number);
            fields.set(number - 1);
        });
        boolean secondary = fields.nextSetBit(64) >= 64;
        BitSet primary = (BitSet) fields.clone();
        if (secondary) primary.set(0); else primary.clear(0);

        StringBuilder output = new StringBuilder(message.mti());
        output.append(hexBitmap(primary, 0));
        if (secondary) output.append(hexBitmap(fields, 64));
        message.fields().entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> output.append(encodeField(entry.getKey(), entry.getValue())));
        return output.toString().getBytes(charset);
    }

    public CpfIso8583Message decode(byte[] data) {
        String input = new String(data, charset);
        if (input.length() < 20) throw new IllegalArgumentException("short ISO8583 message");
        String mti = input.substring(0, 4);
        BitSet primary = parseBitmap(input.substring(4, 20));
        boolean secondary = primary.get(0);
        BitSet fields = new BitSet(128);
        for (int i = primary.nextSetBit(1); i >= 0; i = primary.nextSetBit(i + 1)) fields.set(i);
        int position = 20;
        if (secondary) {
            if (input.length() < 36) throw new IllegalArgumentException("missing ISO8583 secondary bitmap");
            BitSet second = parseBitmap(input.substring(20, 36));
            for (int i = second.nextSetBit(0); i >= 0; i = second.nextSetBit(i + 1)) fields.set(64 + i);
            position = 36;
        }

        Map<Integer, String> values = new LinkedHashMap<>();
        for (int number = 2; number <= 128; number++) {
            if (!fields.get(number - 1)) continue;
            CpfIso8583FieldSpec spec = requireSpec(number);
            int length = spec.maxLength();
            if (spec.format() == CpfIso8583FieldSpec.Format.LLVAR) {
                ensureAvailable(input, position, 2, number);
                length = parseLength(input.substring(position, position + 2), number);
                position += 2;
            } else if (spec.format() == CpfIso8583FieldSpec.Format.LLLVAR) {
                ensureAvailable(input, position, 3, number);
                length = parseLength(input.substring(position, position + 3), number);
                position += 3;
            }
            if (length > spec.maxLength()) throw new IllegalArgumentException("field length exceeds maximum " + number);
            ensureAvailable(input, position, length, number);
            String value = input.substring(position, position + length);
            if (spec.numeric() && !value.matches("\\d+")) throw new IllegalArgumentException("non-numeric field " + number);
            values.put(number, value);
            position += length;
        }
        if (position != input.length()) throw new IllegalArgumentException("trailing ISO8583 data");
        return new CpfIso8583Message(mti, values);
    }

    private String encodeField(int number, String value) {
        CpfIso8583FieldSpec spec = requireSpec(number);
        if (value == null || value.length() > spec.maxLength() || (spec.numeric() && !value.matches("\\d+"))) {
            throw new IllegalArgumentException("invalid field " + number);
        }
        return switch (spec.format()) {
            case FIXED -> {
                if (value.length() != spec.maxLength()) throw new IllegalArgumentException("fixed field length " + number);
                yield value;
            }
            case LLVAR -> String.format(Locale.ROOT, "%02d%s", value.length(), value);
            case LLLVAR -> String.format(Locale.ROOT, "%03d%s", value.length(), value);
        };
    }

    private CpfIso8583FieldSpec requireSpec(int number) {
        CpfIso8583FieldSpec spec = specs.get(number);
        if (spec == null) throw new IllegalArgumentException("missing field spec " + number);
        return spec;
    }

    private static String hexBitmap(BitSet bits, int offset) {
        StringBuilder output = new StringBuilder(16);
        for (int nibble = 0; nibble < 64; nibble += 4) {
            int value = 0;
            for (int bit = 0; bit < 4; bit++) if (bits.get(offset + nibble + bit)) value |= 1 << (3 - bit);
            output.append(Integer.toHexString(value).toUpperCase(Locale.ROOT));
        }
        return output.toString();
    }

    private static BitSet parseBitmap(String hexadecimal) {
        if (hexadecimal.length() != 16) throw new IllegalArgumentException("ISO8583 bitmap must be 16 hex characters");
        BitSet bits = new BitSet(64);
        for (int i = 0; i < hexadecimal.length(); i++) {
            int value = Character.digit(hexadecimal.charAt(i), 16);
            if (value < 0) throw new IllegalArgumentException("invalid bitmap");
            for (int bit = 0; bit < 4; bit++) if ((value & (1 << (3 - bit))) != 0) bits.set(i * 4 + bit);
        }
        return bits;
    }

    private static int parseLength(String value, int field) {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException ex) { throw new IllegalArgumentException("invalid length prefix for field " + field, ex); }
    }

    private static void ensureAvailable(String input, int position, int length, int field) {
        if (position < 0 || length < 0 || position + length > input.length()) {
            throw new IllegalArgumentException("truncated field " + field);
        }
    }
}
