package com.cpf.platform.operations.runtimecontrol;

/** Runtime 변경 적용 시 재기동 영향입니다. */
public enum CpfRuntimeRestartImpact {
    HOT_APPLY,
    RESTART_POSSIBLE,
    RESTART_REQUIRED
}
