package com.cpf.starter.platform.operations.health;

import com.cpf.core.api.health.CpfDependencyHealth;

@FunctionalInterface
public interface CpfDependencyHealthCheck {
    CpfDependencyHealth check();
    default String name() { return getClass().getSimpleName(); }
}
