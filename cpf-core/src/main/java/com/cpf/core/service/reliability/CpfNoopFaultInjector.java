package com.cpf.core.service.reliability;

import com.cpf.core.api.reliability.CpfFaultInjector;

final class CpfNoopFaultInjector implements CpfFaultInjector {
    @Override public void before(String targetId) { }
}
