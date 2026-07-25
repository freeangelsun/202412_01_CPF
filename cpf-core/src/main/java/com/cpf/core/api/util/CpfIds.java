package com.cpf.core.api.util;

import java.util.Locale;
import java.util.UUID;

/** 거래 ID가 아닌 일반 기술 식별자 생성을 제공하는 CPF API입니다. */
public final class CpfIds {
    private CpfIds() {}
    public static String uuid() { return UUID.randomUUID().toString(); }
    public static String compactUuid() { return UUID.randomUUID().toString().replace("-", ""); }
    /** 기존 IdUtils.uuid32와 동일한 의미의 호환 alias입니다. */
    public static String uuid32() { return compactUuid(); }
    public static String normalizeSystemCode(String value) {
        String normalized = CpfStrings.requireText(value, "systemCode").toUpperCase(Locale.ROOT);
        if (!normalized.matches("[A-Z0-9]{3}")) throw new IllegalArgumentException("systemCode는 영문/숫자 3자리여야 합니다.");
        return normalized;
    }
    public static String temporaryId(String prefix) {
        String p = CpfStrings.defaultIfBlank(prefix, "TMP").toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_-]", "");
        if (p.isBlank()) p = "TMP";
        if (p.length() > 12) p = p.substring(0, 12);
        return p + "-" + java.time.Clock.systemUTC().instant().toEpochMilli() + "-" + compactUuid().substring(0, 8);
    }
}
