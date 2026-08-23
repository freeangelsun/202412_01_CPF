package com.cpf.core.api.transaction;

import java.util.regex.Pattern;

/** CPF transactionId 규격을 Framework 외부에서도 동일하게 검사하기 위한 공개 API입니다. */
public final class CpfTransactionIds {
    private static final Pattern CANONICAL = Pattern.compile("^\\d{17}[A-Z0-9]{3}[A-Za-z0-9]{7}\\d{7}$");
    private CpfTransactionIds() {}

    public static boolean isCanonical(String value) {
        return value != null && CANONICAL.matcher(value).matches();
    }

    /** Canonical transactionId를 발급한 3자리 issuer metadata를 반환합니다. 거래 Channel과는 별개입니다. */
    public static String issuerCode(String value) {
        return requireCanonical(value).substring(17, 20);
    }

    /** Canonical transactionId를 발급한 Runtime의 7자리 instance token을 반환합니다. */
    public static String instanceToken(String value) {
        return requireCanonical(value).substring(20, 27);
    }

    /** requireCanonical 작업을 CPF 표준 계약에 따라 수행한다. */
    public static String requireCanonical(String value) {
        if (!isCanonical(value)) {
            throw new IllegalArgumentException("transactionId는 yyyyMMddHHmmssSSS(17)+issuerCode(3)+instanceToken(7)+sequence(7)의 34자리여야 합니다.");
        }
        return value;
    }
}
