package com.cpf.integration.http.internal.servicecall;

import com.cpf.integration.api.servicecall.CpfServiceRegistryQueryPort;
import com.cpf.integration.api.servicecall.CpfServiceRegistryView;

import java.util.List;
import java.util.Objects;

/** CPF Service Registry Owner Repository를 엄격한 Typed Query Port로 변환합니다. */
public final class CpfServiceRegistryQueryFacade implements CpfServiceRegistryQueryPort {
    private final CpfServiceRegistryRepository repository;

    public CpfServiceRegistryQueryFacade(CpfServiceRegistryRepository repository) {
        this.repository = Objects.requireNonNull(repository, "repository");
    }

    @Override
    public List<CpfServiceRegistryView.Service> services(String serviceId, String useYn, int limit) {
        return repository.findServices(serviceId, useYn, normalizeLimit(limit)).stream()
                .map(CpfServiceRegistryView.Service::from).toList();
    }

    @Override
    public List<CpfServiceRegistryView.Endpoint> endpoints(
            String serviceId, String endpointCode, String useYn, int limit) {
        return repository.findEndpoints(serviceId, endpointCode, useYn, normalizeLimit(limit)).stream()
                .map(CpfServiceRegistryView.Endpoint::from).toList();
    }

    @Override
    public List<CpfServiceRegistryView.Instance> instances(
            String serviceId, String endpointCode, String status, int limit) {
        return repository.findInstances(serviceId, endpointCode, status, normalizeLimit(limit)).stream()
                .map(CpfServiceRegistryView.Instance::from).toList();
    }

    @Override
    public List<CpfServiceRegistryView.Health> health(String serviceId, String endpointCode, int limit) {
        return repository.findHealthStatuses(serviceId, endpointCode, normalizeLimit(limit)).stream()
                .map(CpfServiceRegistryView.Health::from).toList();
    }

    @Override
    public List<CpfServiceRegistryView.RoutingPolicy> routingPolicies(
            String serviceId, String endpointCode, String activeYn, int limit) {
        return repository.findRoutingPolicies(serviceId, endpointCode, activeYn, normalizeLimit(limit)).stream()
                .map(CpfServiceRegistryView.RoutingPolicy::from).toList();
    }

    @Override
    public List<CpfServiceRegistryView.CircuitState> circuitStates(
            String serviceId, String endpointCode, int limit) {
        return repository.findCircuitStates(serviceId, endpointCode, normalizeLimit(limit)).stream()
                .map(CpfServiceRegistryView.CircuitState::from).toList();
    }

    @Override
    public List<CpfServiceRegistryView.CallHistory> callHistory(
            String serviceId, String transactionId, int limit) {
        return repository.findCallHistory(serviceId, transactionId, normalizeLimit(limit)).stream()
                .map(CpfServiceRegistryView.CallHistory::from).toList();
    }

    private static int normalizeLimit(int limit) {
        return Math.max(1, Math.min(limit <= 0 ? 100 : limit, 1_000));
    }
}
