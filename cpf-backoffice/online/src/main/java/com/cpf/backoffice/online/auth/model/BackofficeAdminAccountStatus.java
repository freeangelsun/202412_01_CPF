package com.cpf.backoffice.online.auth.model;

import com.cpf.core.api.error.CpfValidationException;

import java.util.Locale;

/** MBW 관리자 인증 계정의 상태 Catalog입니다. 직원 재직 상태와 독립적입니다. */
public enum BackofficeAdminAccountStatus {
    PENDING_ACTIVATION,
    ACTIVE,
    LOCKED,
    SUSPENDED,
    DISABLED;

    public static BackofficeAdminAccountStatus parse(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException error) {
            throw new CpfValidationException("지원하지 않는 관리자 계정 상태입니다.");
        }
    }
}
