package com.cpf.core.api.transaction;

/** TCC branch의 lifecycle 단계입니다. */
public enum CpfTccPhase {
    TRY,
    CONFIRM,
    CANCEL
}
