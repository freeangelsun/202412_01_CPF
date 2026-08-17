package com.cpf.core.api.transaction;

import java.util.regex.Pattern;

/** CPF transactionId 규격을 Framework 외부에서도 동일하게 검사하기 위한 공개 API입니다. */
public final class CpfTransactionIds {
    private static final Pattern CANONICAL = Pattern.compile("^\\d{17}[A-Z0-9]{3}[A-Za-z0-9]{7}\\d{7}$");
    private CpfTransactionIds() {}

    public static boolean isCanonical(String value) {
        return value != null && CANONICAL.matcher(value).matches();
    }

    /** requireCanonical 작업을 CPF 표준 계약에 따라 수행한다. */
    public static String requireCanonical(String value) {
        if (!isCanonical(value)) {
            throw new IllegalArgumentException("transactionId는 yyyyMMddHHmmssSSS(17)+SystemCode(3)+instanceToken(7)+sequence(7)의 34자리여야 합니다.");
        }
        return value;
    }
}
