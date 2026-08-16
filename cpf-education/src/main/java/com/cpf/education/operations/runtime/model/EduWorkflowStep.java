package com.cpf.education.operations.runtime.model;
/** EduWorkflowStep 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public enum EduWorkflowStep {
    VALIDATE, AUTHORIZE, SCOPE, DEDUPE, VERSION_CHECK, PREVIEW, APPROVAL, READ_SNAPSHOT,
    CLAIM_LEASE, PARTITION, CHECKSUM, QUARANTINE, PROTECT, MUTATE, COMMIT,
    CHECKPOINT, OUTBOX, EXTERNAL_SEND, ACK, RECONCILE, COMPENSATION_READY,
    AUDIT, OBSERVE
}
