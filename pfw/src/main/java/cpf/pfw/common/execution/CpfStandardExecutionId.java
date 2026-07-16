package cpf.pfw.common.execution;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * CPF 거래 기반 기능을 식별하는 10자리 표준 실행 ID 값 객체입니다.
 *
 * <p>형식은 {@code 실행유형 1자리 + 실행 주제영역 3자리 + 기능코드 2자리 + 순번 4자리}입니다.
 * URL, 클래스명, 메서드명 또는 배포 인스턴스가 변경되어도 이 값은 업무 계약으로 유지합니다.</p>
 */
public record CpfStandardExecutionId(
        CpfExecutionType type,
        String domain,
        String feature,
        int sequence) {

    public static final Pattern PATTERN = Pattern.compile(
            "^([OSB])([A-Z]{3})([A-Z0-9]{2})([0-9]{4})$");
    public static final Pattern LEGACY_PATTERN = Pattern.compile(
            "^([OB])([A-Z]{3})-([A-Z0-9]{3})-([A-Z0-9]{2})-([0-9]{4})$");

    public CpfStandardExecutionId {
        domain = requireSegment(domain, "domain", "[A-Z]{3}");
        feature = requireSegment(feature, "feature", "[A-Z0-9]{2}");
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
                Integer.parseInt(matcher.group(4)));
    }

    public static boolean isValid(String value) {
        return value != null && PATTERN.matcher(value).matches() && !value.endsWith("0000");
    }

    /**
     * 마이그레이션 조회에서만 허용하는 구형 ID인지 확인합니다.
     *
     * <p>구형 ID는 신규 저장이나 신규 헤더 전파에 사용할 수 없습니다.</p>
     */
    public static boolean isLegacy(String value) {
        return value != null && LEGACY_PATTERN.matcher(value).matches();
    }

    public String value() {
        return "%c%s%s%04d".formatted(type.prefix(), domain, feature, sequence);
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
