package com.cpf.messaging.error;

/** Messaging Runtime의 실패 후 처리 결과입니다. */
public enum CpfMessageFailureAction { ACK, RETRY, DLQ, RECONCILE }
