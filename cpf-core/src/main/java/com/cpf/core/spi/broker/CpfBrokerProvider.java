package com.cpf.core.spi.broker;

import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;

/** Broker provider extension SPI. */
public interface CpfBrokerProvider {
    String providerId();
    CpfBrokerPublishResult publish(CpfBrokerPublishRequest request);
}
