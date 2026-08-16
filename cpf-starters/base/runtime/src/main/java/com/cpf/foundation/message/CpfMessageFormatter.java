package com.cpf.foundation.message;
import java.text.MessageFormat;
import java.util.Map;
/** 명명된 인자를 안정된 순서로 기본 메시지에 적용하는 기술 공통 Helper입니다. */
public final class CpfMessageFormatter {
    private CpfMessageFormatter() { }
    public static String format(String pattern, Map<String, Object> arguments) {
        if (pattern == null || arguments == null || arguments.isEmpty()) return pattern;
        return MessageFormat.format(pattern, arguments.values().toArray());
    }
}
