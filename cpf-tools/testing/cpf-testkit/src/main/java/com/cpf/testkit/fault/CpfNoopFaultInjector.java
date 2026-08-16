package com.cpf.testkit.fault;

import com.cpf.integration.resilience.api.CpfFaultInjector;

final class CpfNoopFaultInjector implements CpfFaultInjector {
    @Override public void before(String targetId) { }
}
