package com.cpf.core.api.util;

import java.util.Locale;
import java.util.UUID;

/** 거래 ID가 아닌 일반 기술 식별자 생성을 제공하는 CPF API입니다. */
public final class CpfIds {
    private CpfIds() {}
    public static String uuid() { return UUID.randomUUID().toString(); }
    public static String compactUuid() { return UUID.randomUUID().toString().replace("-", ""); }
    public static String normalizeSystemCode(String value) {
        String normalized = CpfStrings.requireText(value, "systemCode").toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9]{3}")) throw new IllegalArgumentException("systemCode는 영문/숫자 3자리여야 합니다.");
        return normalized;
    }
}
