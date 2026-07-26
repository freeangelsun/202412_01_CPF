package com.cpf.core.common.system;

import java.util.Locale;
import java.util.Map;

/**
 * CPF 공식 모듈명과 3자리 시스템 코드의 변환 규칙을 제공합니다.
 *
 * <p>설정에는 {@code cpf-core}, {@code core}, {@code CPF}처럼 서로 다른 식별자가
 * 들어올 수 있으므로 거래 ID와 로그를 만들기 전에 이 클래스로 정규화합니다.</p>
 */
public final class CpfSystemCodes {

    public static final String CORE = "CPF";

    private static final int SYSTEM_CODE_LENGTH = 3;
    private static final Map<String, String> OFFICIAL_CODES = Map.ofEntries(
            Map.entry("core", CORE),
            Map.entry("cpf", CORE),
            Map.entry("gateway", "GWY"),
            Map.entry("common", "CMN"),
            Map.entry("admin", "ADM"),
            Map.entry("biz-admin", "BZA"),
            Map.entry("bizadmin", "BZA"),
            Map.entry("batch", "BAT"),
            Map.entry("reference", "REF"));

    private CpfSystemCodes() {
    }

    /**
     * 프로젝트명, 별칭 또는 시스템 코드를 공식 3자리 코드로 정규화합니다.
     * 등록되지 않은 신규 업무 코드는 기존 호환 규칙에 따라 앞 3자리를 사용합니다.
     *
     * @param value 정규화할 모듈 식별자
     * @param fallback 값이 없거나 영문·숫자가 아닐 때 사용할 기본값
     * @return 대문자 3자리 시스템 코드
     */
    public static String normalize(String value, String fallback) {
        String target = hasText(value)
                ? value.trim()
                : (hasText(fallback) ? fallback.trim() : CORE);
        String normalizedTarget = target.toLowerCase(Locale.ROOT);
        if (normalizedTarget.startsWith("cpf-")) {
            normalizedTarget = normalizedTarget.substring("cpf-".length());
        }
        String alias = OFFICIAL_CODES.get(normalizedTarget);
        if (alias != null) {
            return alias;
        }

        String normalized = normalizedTarget.replaceAll("[^a-z0-9]", "").toUpperCase(Locale.ROOT);
        if (!hasText(normalized)) {
            return normalize(fallback, CORE);
        }
        if (normalized.length() >= SYSTEM_CODE_LENGTH) {
            return normalized.substring(0, SYSTEM_CODE_LENGTH);
        }
        return String.format("%-" + SYSTEM_CODE_LENGTH + "s", normalized).replace(' ', 'X');
    }

    /**
     * 공식 Java 패키지명으로 시스템 코드를 추론합니다.
     *
     * @param typeName Controller 또는 Service의 전체 클래스명
     * @return 추론한 시스템 코드, 알 수 없으면 {@code null}
     */
    public static String inferFromTypeName(String typeName) {
        if (!hasText(typeName)) {
            return null;
        }
        String prefix = "com.cpf.";
        if (!typeName.startsWith(prefix)) {
            return null;
        }
        int packageEnd = typeName.indexOf('.', prefix.length());
        String modulePackage = packageEnd < 0
                ? typeName.substring(prefix.length())
                : typeName.substring(prefix.length(), packageEnd);
        return hasText(modulePackage) ? normalize(modulePackage, CORE) : null;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
