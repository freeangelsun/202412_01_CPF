package com.cpf.core.common.broker;

import java.util.Collection;
import java.util.List;

/**
 * CPF broker 발행 port입니다.
 *
 * <p>업무 모듈은 Kafka/MQ/Redis Stream client를 직접 호출하지 않고 이 port를 사용합니다.</p>
 */
public interface CpfBrokerPublisher {

    CpfBrokerResult publish(CpfBrokerEnvelope envelope);

    default List<CpfBrokerResult> publishAll(Collection<CpfBrokerEnvelope> envelopes) {
        if (envelopes == null || envelopes.isEmpty()) {
            return List.of();
        }
        return envelopes.stream()
                .map(this::publish)
                .toList();
    }
}
