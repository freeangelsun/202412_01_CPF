package com.cpf.education.operations.runtime.model;
/** EduFailurePoint 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public enum EduFailurePoint {
    NONE, BEFORE_COMMIT, AFTER_COMMIT, BEFORE_EXTERNAL_SEND, AFTER_EXTERNAL_SEND,
    RESPONSE_LOST, PARTIAL_TARGET_FAILURE, TIMEOUT, PROCESS_KILL, LEASE_LOST
}
