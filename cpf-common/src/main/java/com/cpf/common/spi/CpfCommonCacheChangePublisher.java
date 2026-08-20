package com.cpf.common.spi;

/**
 * Common Product 변경을 durable cache-refresh runtime에 전달하는 공개 경계입니다.
 * Product Source는 Starter runtime 구현에 직접 의존하지 않고 이 계약만 사용합니다.
 */
public interface CpfCommonCacheChangePublisher {
    long publishRequired(String cacheName, String eventType, String eventKey, String actor);
    long publishOutOfBand(String cacheName, String eventType, String eventKey, String actor);
}
