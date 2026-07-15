package cpf.pfw.common.execution;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CPF 온라인·배치 표준 실행 ID 값 객체입니다.
 */
public record CpfStandardExecutionId(
        CpfExecutionType type,
        String domain,
        String business,
        String sub,
        int sequence) {

    public static final Pattern PATTERN = Pattern.compile(
            "^([OB])([A-Z]{3})-([A-Z0-9]{3})-([A-Z0-9]{2})-([0-9]{4})$");

    public CpfStandardExecutionId {
        domain = requireSegment(domain, "domain", "[A-Z]{3}");
        business = requireSegment(business, "business", "[A-Z0-9]{3}");
        sub = requireSegment(sub, "sub", "[A-Z0-9]{2}");
        if (sequence < 1 || sequence > 9999) {
            throw new IllegalArgumentException("표준 실행 ID sequence는 0001~9999여야 합니다.");
        }
    }

    public static CpfStandardExecutionId parse(String value) {
        Matcher matcher = PATTERN.matcher(value == null ? "" : value.trim().toUpperCase(Locale.ROOT));
        if (!matcher.matches()) {
            throw new IllegalArgumentException("표준 실행 ID 형식이 올바르지 않습니다. value=" + value);
        }
        return new CpfStandardExecutionId(
                CpfExecutionType.fromPrefix(matcher.group(1).charAt(0)),
                matcher.group(2),
                matcher.group(3),
                matcher.group(4),
                Integer.parseInt(matcher.group(5)));
    }

    public static boolean isValid(String value) {
        return value != null && PATTERN.matcher(value).matches() && !value.endsWith("-0000");
    }

    public String value() {
        return "%c%s-%s-%s-%04d".formatted(type.prefix(), domain, business, sub, sequence);
    }

    @Override
    public String toString() {
        return value();
    }

    private static String requireSegment(String value, String field, String pattern) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!normalized.matches(pattern)) {
            throw new IllegalArgumentException("표준 실행 ID " + field + " 구간이 올바르지 않습니다. value=" + value);
        }
        return normalized;
    }
}
