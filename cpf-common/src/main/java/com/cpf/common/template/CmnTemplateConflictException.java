package com.cpf.common.template;

/** Fail-closed conflict for duplicate versions, invalid transitions and stale revisions. */
public final class CmnTemplateConflictException extends RuntimeException {
    /** Template 변경 충돌의 원인을 분류해 안전한 재시도/사용자 안내에 사용하는 유형입니다. */
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
