package com.cpf.backoffice.online.management.model;

import com.cpf.core.api.error.CpfValidationException;

import java.util.Locale;
import java.util.Set;

/** 직원의 재직 상태 Catalog입니다. 계정 활성 상태와 혼용하지 않습니다. */
public enum BackofficeEmploymentStatus {
    EMPLOYED,
    ON_LEAVE,
    SECONDMENT,
    DISPATCHED,
    RETIRED,
    TERMINATED;

    private static final Set<BackofficeEmploymentStatus> BUSINESS_ACTIVE =
            Set.of(EMPLOYED, SECONDMENT, DISPATCHED);

    public static BackofficeEmploymentStatus parse(String value) {
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException error) {
            throw new CpfValidationException("지원하지 않는 재직 상태입니다.");
        }
    }

    public boolean businessActive() {
        return BUSINESS_ACTIVE.contains(this);
    }
}
