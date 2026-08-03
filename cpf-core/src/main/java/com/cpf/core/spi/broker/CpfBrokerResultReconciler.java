package com.cpf.core.spi.broker;

import com.cpf.core.api.broker.CpfBrokerPublishResult;
import com.cpf.core.api.broker.CpfBrokerPublishResultProbe;

/** Provider extension for resolving ambiguous publish outcomes. */
public interface CpfBrokerResultReconciler {
    CpfBrokerPublishResult reconcile(CpfBrokerPublishResultProbe probe);
}
