package com.cpf.platform.operations.runtimecontrol.internal;

import com.cpf.platform.operations.runtimecontrol.api.CpfManagedRuntimeRegistry;
import com.cpf.platform.operations.runtimecontrol.api.CpfManagedRuntimeSnapshot;
import java.time.Duration;
import java.util.List;

/** 중앙 cpfDB Runtime Registry의 Public Port provider입니다. */
public final class CpfJdbcManagedRuntimeRegistry implements CpfManagedRuntimeRegistry {
    private final CpfRuntimeControlPlaneRepository repository;

    public CpfJdbcManagedRuntimeRegistry(CpfRuntimeControlPlaneRepository repository) {
        this.repository = repository;
    }

    @Override public CpfManagedRuntimeSnapshot snapshot(String instanceId) {
        return repository.managedRuntimeSnapshot(instanceId);
    }

    @Override public List<CpfManagedRuntimeSnapshot> list(Duration staleAfter) {
        return repository.managedRuntimeList(staleAfter);
    }

    @Override public long updateDesiredState(String instanceId, String desiredState, long expectedVersion) {
        return repository.updateManagedDesiredState(instanceId, desiredState, expectedVersion);
    }

    @Override public void reportActualState(String instanceId, String actualState) {
        repository.reportManagedActualState(instanceId, actualState);
    }
}
