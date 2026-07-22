package cpf.pfw.common.exception;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
public final class CpfMessageFormatter {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([A-Za-z0-9_.-]+)}");

    private CpfMessageFormatter() {
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
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
     * Formats indexed placeholders such as {@code {0}}, {@code {1}}.
     *
     * <p>This is the standard CPF message format for DB-managed common messages.
     * The map based formatter remains supported for existing named placeholders.</p>
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

