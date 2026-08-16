package com.cpf.data.persistence.jpa;

import com.cpf.data.persistence.api.CpfLockMode;
import jakarta.persistence.LockModeType;

/** CPF lock 의도를 JPA 표준 LockModeType으로 변환합니다. */
public final class CpfJpaLockModes {
    private CpfJpaLockModes() { }

    public static LockModeType toJpa(CpfLockMode mode) {
        return switch (mode) {
            case NONE -> LockModeType.NONE;
            case OPTIMISTIC -> LockModeType.OPTIMISTIC;
            case OPTIMISTIC_FORCE_INCREMENT -> LockModeType.OPTIMISTIC_FORCE_INCREMENT;
            case PESSIMISTIC_READ -> LockModeType.PESSIMISTIC_READ;
            case PESSIMISTIC_WRITE -> LockModeType.PESSIMISTIC_WRITE;
        };
    }
}
