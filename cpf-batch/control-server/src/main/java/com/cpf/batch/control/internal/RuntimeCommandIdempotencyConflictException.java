package com.cpf.batch.control.internal;

/** Raised when a command primary key is already bound to another idempotency key. */
public final class RuntimeCommandIdempotencyConflictException extends RuntimeException {
    public RuntimeCommandIdempotencyConflictException(String message, Throwable cause) {
        super(message, cause);
    }
}
