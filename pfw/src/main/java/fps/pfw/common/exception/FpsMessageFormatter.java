package fps.pfw.common.exception;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FPS 메시지 템플릿의 동적 값을 치환하는 공통 포맷터입니다.
 *
 * <p>CMN 메시지 테이블이나 예외 생성자에서 {@code {fieldName}} 같은 플레이스홀더를 사용하면,
 * 표준 예외 처리와 거래 로그가 같은 규칙으로 메시지를 조립합니다.</p>
 */
public final class FpsMessageFormatter {
    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([A-Za-z0-9_.-]+)}");

    private FpsMessageFormatter() {
    }

    /**
     * 메시지 템플릿 안의 {@code {key}} 값을 인자 맵의 값으로 치환합니다.
     *
     * @param template 메시지 템플릿
     * @param arguments 치환할 메시지 인자
     * @return 치환된 메시지. 인자가 없거나 키가 없으면 원래 플레이스홀더를 유지합니다.
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
}
