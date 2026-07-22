package com.cpf.core.common.logging.segment;

/**
 * 거래 구간이 요청 흐름에서 어느 방향의 처리를 나타내는지 구분합니다.
 */
public enum TransactionSegmentDirection {
    INBOUND,
    OUTBOUND,
    INTERNAL,
    RESPONSE
}
