package fps.cmn.utils;

import java.util.Locale;

/**
 * 문자열 처리 공통 유틸리티입니다.
 *
 * <p>업무 개발자가 반복적으로 작성하는 null/blank 검사, 기본값 처리, 코드 정규화 로직을
 * 한 곳에서 사용하도록 제공합니다. 업무 의미가 들어간 변환은 이 클래스에 넣지 않고
 * 각 업무 도메인 서비스에 둡니다.</p>
 */
public final class TextUtils {

    private TextUtils() {
    }

    /**
     * 문자열이 null이 아니고 공백만으로 구성되지 않았는지 확인합니다.
     *
     * @param value 검사할 문자열
     * @return 값이 있으면 true
     */
    public static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    /**
     * 값이 없으면 기본값을 반환합니다.
     *
     * @param value        원본 문자열
     * @param defaultValue 기본 문자열
     * @return 원본에 값이 있으면 원본, 없으면 기본값
     */
    public static String defaultIfBlank(String value, String defaultValue) {
        return hasText(value) ? value : defaultValue;
    }

    /**
     * 필수 문자열을 검증하고 앞뒤 공백을 제거해 반환합니다.
     *
     * @param value     원본 문자열
     * @param fieldName 오류 메시지에 사용할 필드명
     * @return trim 처리된 문자열
     * @throws IllegalArgumentException 값이 없을 때 발생
     */
    public static String requireText(String value, String fieldName) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(fieldName + " 값은 필수입니다.");
        }
        return value.trim();
    }

    /**
     * 코드값 비교를 위해 공백 제거 후 대문자로 변환합니다.
     *
     * @param value 원본 코드값
     * @return 정규화된 코드값
     */
    public static String normalizeCode(String value) {
        return hasText(value) ? value.trim().toUpperCase(Locale.ROOT) : "";
    }
}
