package com.cpf.core.api.transaction;

/**
 * CPF가 공식 지원하는 거래 일관성 전략입니다.
 * 전략은 상호 대체 관계가 아니며 업무 경계에 따라 조합할 수 있습니다.
 */
public enum CpfTransactionStrategy {
    LOCAL,
    XA_JTA,
    OUTBOX,
    INBOX_DEDUP,
    SAGA,
    TCC
}
