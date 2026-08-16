package com.cpf.reliability.runtime;

import com.cpf.reliability.api.CpfIdempotencyException;
import com.cpf.reliability.api.CpfIdempotencyResultCodec;
import com.cpf.reliability.api.CpfIdempotencyStore;
import java.nio.charset.StandardCharsets;

/** Java 직렬화 없이 void/문자열/원시 wrapper/enum/byte[]만 재생하는 안전 기본 codec입니다. */
public final class CpfScalarIdempotencyResultCodec implements CpfIdempotencyResultCodec {
    @Override public boolean supports(Class<?> type) {
        return type == void.class || type == Void.class || type == String.class || type == byte[].class
                || type == boolean.class || type == Boolean.class || type == byte.class || type == Byte.class
                || type == short.class || type == Short.class || type == int.class || type == Integer.class
                || type == long.class || type == Long.class || type == float.class || type == Float.class
                || type == double.class || type == Double.class || type == char.class || type == Character.class
                || type.isEnum();
    }
    @Override public CpfIdempotencyStore.StoredResult encode(Object value, Class<?> type) {
        if (!supports(type)) throw unsupported(type);
        if (type == void.class || type == Void.class) return new CpfIdempotencyStore.StoredResult("scalar-v1", new byte[0], "void");
        if (type == byte[].class) return new CpfIdempotencyStore.StoredResult("bytes-v1", value == null ? new byte[0] : (byte[]) value, type.getName());
        return new CpfIdempotencyStore.StoredResult("scalar-v1",
                String.valueOf(value).getBytes(StandardCharsets.UTF_8), type.getName());
    }
    @Override public Object decode(CpfIdempotencyStore.StoredResult stored, Class<?> type) {
        if (!supports(type)) throw unsupported(type);
        if (type == void.class || type == Void.class) return null;
        if (type == byte[].class) return stored.payload();
        String value = new String(stored.payload(), StandardCharsets.UTF_8);
        if (type == String.class) return value;
        if (type == boolean.class || type == Boolean.class) return Boolean.valueOf(value);
        if (type == byte.class || type == Byte.class) return Byte.valueOf(value);
        if (type == short.class || type == Short.class) return Short.valueOf(value);
        if (type == int.class || type == Integer.class) return Integer.valueOf(value);
        if (type == long.class || type == Long.class) return Long.valueOf(value);
        if (type == float.class || type == Float.class) return Float.valueOf(value);
        if (type == double.class || type == Double.class) return Double.valueOf(value);
        if (type == char.class || type == Character.class) {
            if (value.length() != 1) throw new CpfIdempotencyException("CPF_IDEMPOTENCY_REPLAY_CORRUPT", "Invalid char payload");
            return value.charAt(0);
        }
        if (type.isEnum()) {
            @SuppressWarnings({"rawtypes", "unchecked"}) Object e = Enum.valueOf((Class<? extends Enum>) type.asSubclass(Enum.class), value);
            return e;
        }
        throw unsupported(type);
    }
    private static CpfIdempotencyException unsupported(Class<?> type) {
        return new CpfIdempotencyException("CPF_IDEMPOTENCY_CODEC_REQUIRED", "No idempotency result codec for " + type.getName());
    }
}
