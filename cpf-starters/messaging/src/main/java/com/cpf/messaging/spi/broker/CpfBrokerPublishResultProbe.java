package com.cpf.messaging.spi.broker;

/** Provider extension SPI for resolving an UNKNOWN publish result without duplicate publication. */
@FunctionalInterface
/** CpfBrokerPublishResultProbe는 Broker 처리 결과의 확인·복구 경계를 외부 구현과 분리하는 SPI입니다. */
public interface CpfBrokerPublishResultProbe {
    CpfBrokerResult probe(CpfBrokerEnvelope envelope);
}
