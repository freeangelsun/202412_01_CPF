package com.cpf.common.template;

/** Fail-closed conflict for duplicate versions, invalid transitions and stale revisions. */
public final class CmnTemplateConflictException extends RuntimeException {
    public enum Type { VERSION_EXISTS, NOT_FOUND, INVALID_STATE, REVISION_CONFLICT }

    private final Type type;

    public CmnTemplateConflictException(Type type, String message) {
        super(message);
        this.type = type;
    }

    public Type type() {
        return type;
    }
}
