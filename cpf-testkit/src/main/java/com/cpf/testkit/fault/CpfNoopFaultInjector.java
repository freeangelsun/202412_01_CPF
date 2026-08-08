package com.cpf.testkit.fault;

import com.cpf.core.api.reliability.CpfFaultInjector;

final class CpfNoopFaultInjector implements CpfFaultInjector {
    @Override public void before(String targetId) { }
}
