package cpf.cmn.edu.validation;

import java.util.regex.Pattern;

/**
 * 프로젝트 공통 validation helper 샘플입니다.
 */
public class CmnValidationEducationSample {
    private static final Pattern BUSINESS_KEY = Pattern.compile("[A-Z0-9_]{3,30}");

    public String validateBusinessKey(String value) {
        if (value == null || !BUSINESS_KEY.matcher(value).matches()) {
            throw new IllegalArgumentException("업무키는 영문 대문자/숫자/_ 3~30자리여야 합니다.");
        }
        return value;
    }
}
