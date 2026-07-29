package com.cpf.core.api.error;

import java.util.Map;

/** CPF 메시지 자리표시자 치환을 노출하는 공개 stateless facade입니다. */
public final class CpfMessageFormatter {
    private CpfMessageFormatter() {
    }

    public static String format(String template, Map<String, Object> arguments) {
        return com.cpf.core.common.exception.CpfMessageFormatter.format(template, arguments);
    }

    public static String format(String template, Object... arguments) {
        return com.cpf.core.common.exception.CpfMessageFormatter.format(template, arguments);
    }
}
