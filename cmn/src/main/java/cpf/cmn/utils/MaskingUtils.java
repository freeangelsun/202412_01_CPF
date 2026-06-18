package cpf.cmn.utils;

import cpf.pfw.common.logging.SensitiveDataMasker;

/**
 * 개인정보와 민감값 마스킹 공통 유틸리티입니다.
 *
 * <p>실제 마스킹 규칙은 PFW의 {@link SensitiveDataMasker}를 사용합니다.
 * 업무 개발자는 로그나 샘플 응답에서 민감값을 직접 다루기보다 이 유틸리티를 통해
 * 프레임워크 기준과 같은 규칙을 적용합니다.</p>
 */
public final class MaskingUtils {

    private MaskingUtils() {
    }

    /**
     * PFW 민감정보 마스킹 규칙으로 문자열을 마스킹합니다.
     *
     * @param value 원본 문자열
     * @return 마스킹된 문자열
     */
    public static String maskSensitive(String value) {
        return SensitiveDataMasker.mask(value);
    }

    /**
     * 이름의 가운데 영역을 마스킹합니다.
     *
     * @param name 이름
     * @return 마스킹된 이름
     */
    public static String maskName(String name) {
        if (!TextUtils.hasText(name)) {
            return "";
        }
        String trimmed = name.trim();
        if (trimmed.length() <= 1) {
            return "*";
        }
        if (trimmed.length() == 2) {
            return trimmed.charAt(0) + "*";
        }
        return trimmed.charAt(0) + "*".repeat(trimmed.length() - 2) + trimmed.charAt(trimmed.length() - 1);
    }
}

