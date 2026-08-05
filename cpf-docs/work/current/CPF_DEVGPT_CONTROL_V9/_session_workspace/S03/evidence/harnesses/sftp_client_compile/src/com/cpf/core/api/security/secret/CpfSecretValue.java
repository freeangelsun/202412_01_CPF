package com.cpf.core.api.security.secret;

import java.util.Arrays;

/**
 * Secret 원문을 char[]로 보관하고 사용 후 zeroing 할 수 있는 값 객체.
 * 로그/toString에서 값이 노출되지 않습니다.
 */
public final class CpfSecretValue implements AutoCloseable {
    private char[] value;

    public CpfSecretValue(char[] value) {
        if (value == null || value.length == 0) throw new IllegalArgumentException("secret value는 비어 있을 수 없습니다.");
        this.value = Arrays.copyOf(value, value.length);
    }

    public char[] copy() {
        if (value == null) throw new IllegalStateException("Secret은 이미 폐기되었습니다.");
        return Arrays.copyOf(value, value.length);
    }

    @Override public void close() {
        if (value != null) {
            Arrays.fill(value, '\0');
            value = null;
        }
    }

    @Override public String toString() { return "[REDACTED]"; }
}
