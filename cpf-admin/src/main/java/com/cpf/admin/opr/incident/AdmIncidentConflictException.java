package com.cpf.admin.opr.incident;

/** Incident lifecycle의 조회·CAS·멱등·상태전이 충돌을 HTTP 계약으로 분류합니다. */
public final class AdmIncidentConflictException extends RuntimeException {
    public enum Type { NOT_FOUND, VERSION_CONFLICT, ACTIVE_CONFLICT, INVALID_TRANSITION, IDEMPOTENCY_CONFLICT, COMMAND_IN_PROGRESS }
    private final Type type;

    public AdmIncidentConflictException(Type type, String message) {
        super(message);
        this.type = type;
    }

    public Type type() { return type; }
}
