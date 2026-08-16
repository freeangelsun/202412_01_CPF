package com.cpf.platform.operations.health;
import com.cpf.platform.operations.api.health.CpfDependencyHealth;
@FunctionalInterface
public interface CpfDependencyHealthCheck {
    CpfDependencyHealth check();
    default String name() { return getClass().getSimpleName(); }
}
