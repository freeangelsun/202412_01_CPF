package com.cpf.core.common.logging;

/** Java21 transaction harness용 legacy masking stub. Product Source가 아니다. */
public final class SensitiveDataMasker {
    private SensitiveDataMasker() {}
    public static String mask(String value) { return "***"; }
}
