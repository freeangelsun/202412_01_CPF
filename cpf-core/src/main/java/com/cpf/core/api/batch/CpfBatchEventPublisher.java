package com.cpf.core.api.batch;

/**
 * <p>이 타입은 BAT Runtime 구현과 분리된 CPF 공개 Batch 계약입니다.</p>
 * 배치 이벤트 전파 어댑터입니다.
 *
 * <p>운영 환경에서는 Redis/Kafka 구현체로 교체하고, 로컬 환경에서는 로그 fallback으로 동작할 수 있게
 * 공통 인터페이스를 둡니다.</p>
 */
public interface CpfBatchEventPublisher {

    /**
     * publish 운영 조작을 수행합니다. 구현체는 인증 주체, 승인, 멱등성, 동시성, 감사와 UNKNOWN/복구 의미를 보존해야 합니다.
     * @param event 발행할 배치 이벤트입니다. null은 허용하지 않아야 합니다.
     */
    void publish(CpfBatchEvent event);
}
