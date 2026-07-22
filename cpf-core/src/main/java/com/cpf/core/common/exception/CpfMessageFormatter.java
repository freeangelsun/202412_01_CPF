package com.cpf.core.common.exception;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** CPF 공통 메시지의 이름 기반·순번 기반 치환식을 처리합니다. */
public final class CpfMessageFormatter {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([A-Za-z0-9_.-]+)}");

    private CpfMessageFormatter() {
    }

    /** {@code {fieldName}} 형식의 이름 기반 자리표시자를 Map 값으로 치환합니다. */
    public static String format(String template, Map<String, Object> arguments) {
        if (template == null || template.isBlank() || arguments == null || arguments.isEmpty()) {
            return template;
        }

        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuffer formatted = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = arguments.get(key);
            if (value == null) {
                matcher.appendReplacement(formatted, Matcher.quoteReplacement(matcher.group()));
            } else {
                matcher.appendReplacement(formatted, Matcher.quoteReplacement(String.valueOf(value)));
            }
        }
        matcher.appendTail(formatted);
        return formatted.toString();
    }

    /**
     * {@code {0}}, {@code {1}} 형식의 순번 기반 자리표시자를 치환합니다.
     *
     * <p>DB에서 관리하는 CPF 공통 메시지의 기본 형식이며, 기존 이름 기반 형식도
     * 별도 오버로드로 계속 지원합니다.</p>
     */
    public static String format(String template, Object... arguments) {
        if (template == null || template.isBlank() || arguments == null || arguments.length == 0) {
            return template;
        }

        String formatted = template;
        for (int index = 0; index < arguments.length; index++) {
            Object value = arguments[index];
            if (value != null) {
                formatted = formatted.replace("{" + index + "}", String.valueOf(value));
            }
        }
        return formatted;
    }
}

