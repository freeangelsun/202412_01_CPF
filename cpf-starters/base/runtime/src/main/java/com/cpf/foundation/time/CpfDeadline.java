package com.cpf.foundation.time;

/** Monotonic clock 기반 만료 시점을 표현하여 wall-clock 변경에 영향을 받지 않습니다. */
public record CpfDeadline(long expiresAtNanos) {
    public boolean expired(CpfTimeOperations time) {
        return time.monotonicNanos() >= expiresAtNanos;
    }
}
