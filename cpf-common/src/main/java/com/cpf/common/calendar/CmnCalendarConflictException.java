package com.cpf.common.calendar;

/** Calendar 생성/수정/삭제의 동시성 충돌을 구분하는 예외입니다. */
public class CmnCalendarConflictException extends RuntimeException {
    /** Calendar 변경 충돌의 원인을 분류해 409/업무 오류 처리에 사용하는 유형입니다. */
    public enum Type { CREATE_CONFLICT, VERSION_CONFLICT, DELETE_CONFLICT, NOT_FOUND }
    private final Type type;
    public CmnCalendarConflictException(Type type, String message) { super(message); this.type = type; }
    public Type type() { return type; }
}
