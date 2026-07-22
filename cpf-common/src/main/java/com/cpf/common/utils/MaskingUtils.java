package com.cpf.common.utils;

import com.cpf.core.common.logging.SensitiveDataMasker;

/**
 * 개인정보와 민감값 마스킹 공통 유틸리티입니다.
 *
 * <p>실제 마스킹 규칙은 CPF의 {@link SensitiveDataMasker}를 사용합니다.
 * 업무 개발자는 로그나 샘플 응답에서 민감값을 직접 다루기보다 이 유틸리티를 통해
 * 프레임워크 기준과 같은 규칙을 적용합니다.</p>
 */
public final class MaskingUtils {

    private MaskingUtils() {
    }

    /**
     * CPF 민감정보 마스킹 규칙으로 문자열을 마스킹합니다.
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

    /**
     * 이메일 local-part의 뒤쪽을 마스킹합니다.
     *
     * @param email 이메일
     * @return 마스킹된 이메일
     */
    public static String maskEmail(String email) {
        if (!TextUtils.hasText(email)) {
            return "";
        }
        String trimmed = email.trim();
        int atIndex = trimmed.indexOf('@');
        if (atIndex <= 1) {
            return "***" + (atIndex >= 0 ? trimmed.substring(atIndex) : "");
        }
        String localPart = trimmed.substring(0, atIndex);
        String domain = trimmed.substring(atIndex);
        int visibleLength = Math.min(2, localPart.length());
        return localPart.substring(0, visibleLength) + "****" + domain;
    }

    /**
     * 휴대폰 번호의 가운데 영역을 마스킹합니다.
     *
     * @param mobileNo 휴대폰 번호
     * @return 마스킹된 휴대폰 번호
     */
    public static String maskMobile(String mobileNo) {
        if (!TextUtils.hasText(mobileNo)) {
            return "";
        }
        String trimmed = mobileNo.trim();
        if (trimmed.length() <= 4) {
            return "****";
        }
        String digits = trimmed.replaceAll("[^0-9]", "");
        if (digits.length() >= 10) {
            return digits.substring(0, 3) + "-****-" + digits.substring(digits.length() - 4);
        }
        return trimmed.substring(0, Math.min(2, trimmed.length())) + "****" + trimmed.substring(trimmed.length() - 2);
    }
}

