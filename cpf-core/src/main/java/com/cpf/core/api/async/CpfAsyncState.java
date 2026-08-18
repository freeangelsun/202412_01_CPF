package com.cpf.core.api.async;
/** 범용 Async Operation 자체의 lifecycle입니다. Batch/File/Worker 상태와 합치지 않습니다. */
public enum CpfAsyncState {
    ACCEPTED, RUNNING, SUCCEEDED, FAILED, UNKNOWN, CANCEL_REQUESTED, CANCELLED, EXPIRED;
    public boolean terminal() { return this == SUCCEEDED || this == FAILED || this == UNKNOWN || this == CANCELLED || this == EXPIRED; }
}
