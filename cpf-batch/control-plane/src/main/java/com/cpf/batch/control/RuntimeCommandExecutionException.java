package com.cpf.batch.control;

import com.cpf.batch.api.CommandState;

/** Stable error raised when a Runtime command result cannot be safely reported. */
public final class RuntimeCommandExecutionException extends RuntimeException {
    private final String code;
    private final CommandState state;

    public RuntimeCommandExecutionException(String code, CommandState state, String message) {
        super(message);
        this.code = code;
        this.state = state;
    }

    public RuntimeCommandExecutionException(
            String code, CommandState state, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.state = state;
    }

    public String code() {
        return code;
    }

    public CommandState state() {
        return state;
    }
}
