package com.cpf.core.common.broker;

/** Provider extension SPI for resolving an UNKNOWN publish result without duplicate publication. */
@FunctionalInterface
public interface CpfBrokerPublishResultProbe {
    CpfBrokerResult probe(CpfBrokerEnvelope envelope);
}
