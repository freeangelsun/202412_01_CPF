package com.cpf.integration.fixedlength;

import java.math.BigInteger;
import java.nio.ByteOrder;
import java.util.HexFormat;
import java.util.Objects;

/** Strict binary field codec for unsigned integers, endian conversion, packed BCD and hexadecimal fields. */
public final class CpfBinaryFieldCodec {

    public byte[] unsigned(long value, int width, ByteOrder order) {
        Objects.requireNonNull(order, "order");
        if (width < 1 || width > 8 || value < 0) {
            throw new IllegalArgumentException("unsigned width/value");
        }
        BigInteger max = BigInteger.ONE.shiftLeft(width * 8);
        BigInteger number = BigInteger.valueOf(value);
        if (number.compareTo(max) >= 0) {
            throw new IllegalArgumentException("unsigned overflow");
        }
        byte[] output = new byte[width];
        for (int i = 0; i < width; i++) {
            int index = order == ByteOrder.BIG_ENDIAN ? width - 1 - i : i;
            output[index] = (byte) (value >>> (i * 8));
        }
        return output;
    }

    public long unsigned(byte[] bytes, ByteOrder order) {
        Objects.requireNonNull(bytes, "bytes");
        Objects.requireNonNull(order, "order");
        if (bytes.length < 1 || bytes.length > 8) {
            throw new IllegalArgumentException("unsigned width");
        }
        long value = 0;
        for (int i = 0; i < bytes.length; i++) {
            int index = order == ByteOrder.BIG_ENDIAN ? i : bytes.length - 1 - i;
            value = (value << 8) | (bytes[index] & 0xffL);
        }
        if (value < 0) {
            throw new ArithmeticException("unsigned value exceeds signed long");
        }
        return value;
    }

    public byte[] packedBcd(String digits) {
        if (digits == null || !digits.matches("[0-9]+")) {
            throw new IllegalArgumentException("BCD digits required");
        }
        String normalized = digits.length() % 2 == 0 ? digits : "0" + digits;
        byte[] output = new byte[normalized.length() / 2];
        for (int i = 0; i < output.length; i++) {
            output[i] = (byte) (((normalized.charAt(i * 2) - '0') << 4)
                    | (normalized.charAt(i * 2 + 1) - '0'));
        }
        return output;
    }

    public String packedBcd(byte[] bytes, int digits) {
        Objects.requireNonNull(bytes, "bytes");
        if (bytes.length == 0 || digits < 1 || digits > bytes.length * 2) {
            throw new IllegalArgumentException("invalid BCD");
        }
        StringBuilder builder = new StringBuilder(bytes.length * 2);
        for (byte value : bytes) {
            int high = (value >>> 4) & 0xf;
            int low = value & 0xf;
            if (high > 9 || low > 9) {
                throw new IllegalArgumentException("invalid BCD nibble");
            }
            builder.append((char) ('0' + high)).append((char) ('0' + low));
        }
        String decoded = builder.toString();
        return decoded.substring(decoded.length() - digits);
    }

    public byte[] hex(String value) {
        Objects.requireNonNull(value, "value");
        try {
            return HexFormat.of().parseHex(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("invalid hexadecimal field", ex);
        }
    }

    public String hex(byte[] value) {
        Objects.requireNonNull(value, "value");
        return HexFormat.of().withUpperCase().formatHex(value);
    }
}
