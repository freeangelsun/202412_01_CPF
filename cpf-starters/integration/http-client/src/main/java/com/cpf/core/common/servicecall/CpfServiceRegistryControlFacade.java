package com.cpf.core.common.servicecall;

import com.cpf.core.api.servicecall.CpfServiceRegistryControlPort;
import com.cpf.core.api.servicecall.CpfServiceRegistryView;

import java.util.Map;
import java.util.Objects;

/** Service Registry Owner 내부 Repository 결과를 Typed Command Result로 변환합니다. */
public final class CpfServiceRegistryControlFacade implements CpfServiceRegistryControlPort {
    private final CpfServiceRegistryRepository repository;

    public CpfServiceRegistryControlFacade(CpfServiceRegistryRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    @Override
    public CpfServiceRegistryView.MutationResult saveService(ServiceDefinition command) {
        Map<String, Object> row = repository.saveService(command);
        return CpfServiceRegistryView.MutationResult.applied(
                "SERVICE", command.serviceId(), command.operationId(), row);
    }

    @Override
    public CpfServiceRegistryView.MutationResult saveEndpoint(EndpointDefinition command) {
        Map<String, Object> row = repository.saveEndpoint(command);
        return CpfServiceRegistryView.MutationResult.applied(
                "ENDPOINT", command.endpointCode(), command.operationId(), row);
    }

    @Override
    public CpfServiceRegistryView.MutationResult saveInstance(InstanceDefinition command) {
        Map<String, Object> row = repository.saveInstance(command);
        return CpfServiceRegistryView.MutationResult.applied(
                "INSTANCE", command.instanceId(), command.operationId(), row);
    }

    @Override
    public CpfServiceRegistryView.MutationResult deleteService(String serviceId, DeleteCommand command) {
        repository.deleteService(serviceId, command);
        return new CpfServiceRegistryView.MutationResult(
                "SERVICE", serviceId, command.operationId(), "DELETED",
                command.expectedVersion() + 1, java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));
    }

    @Override
    public CpfServiceRegistryView.MutationResult deleteEndpoint(String endpointCode, DeleteCommand command) {
        repository.deleteEndpoint(endpointCode, command);
        return new CpfServiceRegistryView.MutationResult(
                "ENDPOINT", endpointCode, command.operationId(), "DELETED",
                command.expectedVersion() + 1, java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));
    }

    @Override
    public CpfServiceRegistryView.MutationResult deleteInstance(String instanceId, DeleteCommand command) {
        repository.deleteInstance(instanceId, command);
        return new CpfServiceRegistryView.MutationResult(
                "INSTANCE", instanceId, command.operationId(), "DELETED",
                command.expectedVersion() + 1, java.time.OffsetDateTime.now(java.time.ZoneOffset.UTC));
    }

    @Override
    public CpfServiceRegistryView.MutationResult changeInstanceState(
            String serviceId, String endpointCode, String instanceId, InstanceStateCommand command) {
        Map<String, Object> row = repository.changeInstanceState(
                serviceId, endpointCode, instanceId, command);
        return CpfServiceRegistryView.MutationResult.stateChanged(
                instanceId, command.operationId(), "APPLIED", row);
    }
}
