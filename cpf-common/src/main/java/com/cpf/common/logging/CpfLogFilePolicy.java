package com.cpf.common.logging;

import java.util.Locale;

/**
 * 하나의 Application Runtime 로그파일에 적용할 개발자 가시적 정책입니다.
 * Transaction Evidence 로그의 별도 lifecycle과 혼용하지 않습니다.
 */
public record CpfLogFilePolicy(
        boolean enabled,
        String fileName,
        String level,
        Rolling rolling,
        int compressAfterDays,
        int deleteAfterDays) {

    /** CPF 일반 File Log가 지원하는 rolling 기준입니다. */
    public enum Rolling { DAILY }

    public CpfLogFilePolicy {
        level = level == null || level.isBlank() ? null : level.trim().toUpperCase(Locale.ROOT);
    }
}
