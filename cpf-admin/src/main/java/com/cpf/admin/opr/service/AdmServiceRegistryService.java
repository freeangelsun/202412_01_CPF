package com.cpf.admin.opr.service;

import com.cpf.core.api.servicecall.CpfServiceRegistryControlPort;
import com.cpf.core.api.servicecall.CpfServiceRegistryQueryPort;
import com.cpf.core.api.servicecall.CpfServiceRegistryView;
import org.springframework.stereotype.Service;

import java.util.List;

/** ADM은 Registry 저장소를 직접 수정하지 않고 공개 Query/Command Port만 사용합니다. */
@Service
public class AdmServiceRegistryService extends com.cpf.admin.common.base.AdmBaseService {
    private final CpfServiceRegistryQueryPort queryPort;
    private final CpfServiceRegistryControlPort controlPort;

    public AdmServiceRegistryService(CpfServiceRegistryQueryPort queryPort, CpfServiceRegistryControlPort controlPort) {
        this.queryPort = queryPort;
        this.controlPort = controlPort;
    }

    public List<CpfServiceRegistryView.Service> findServices(String serviceId, String useYn, int limit) {
        return queryPort.services(serviceId, useYn, limit);
    }
    public List<CpfServiceRegistryView.Endpoint> findEndpoints(String serviceId, String endpointCode, String useYn, int limit) {
        return queryPort.endpoints(serviceId, endpointCode, useYn, limit);
    }
    public List<CpfServiceRegistryView.Instance> findInstances(String serviceId, String endpointCode, String status, int limit) {
        return queryPort.instances(serviceId, endpointCode, status, limit);
    }
    public List<CpfServiceRegistryView.Health> findHealth(String serviceId, String endpointCode, int limit) {
        return queryPort.health(serviceId, endpointCode, limit);
    }
    public List<CpfServiceRegistryView.RoutingPolicy> findRoutingPolicies(String serviceId, String endpointCode, String activeYn, int limit) {
        return queryPort.routingPolicies(serviceId, endpointCode, activeYn, limit);
    }
    public List<CpfServiceRegistryView.CircuitState> findCircuitStates(String serviceId, String endpointCode, int limit) {
        return queryPort.circuitStates(serviceId, endpointCode, limit);
    }
    public List<CpfServiceRegistryView.CallHistory> findCallHistory(String serviceId, String transactionId, int limit) {
        return queryPort.callHistory(serviceId, transactionId, limit);
    }
    public CpfServiceRegistryView.MutationResult saveService(CpfServiceRegistryControlPort.ServiceDefinition command) {
        return controlPort.saveServiceTyped(command);
    }
    public CpfServiceRegistryView.MutationResult saveEndpoint(CpfServiceRegistryControlPort.EndpointDefinition command) {
        return controlPort.saveEndpointTyped(command);
    }
    public CpfServiceRegistryView.MutationResult saveInstance(CpfServiceRegistryControlPort.InstanceDefinition command) {
        return controlPort.saveInstanceTyped(command);
    }
    public CpfServiceRegistryView.MutationResult changeInstanceState(
            String serviceId, String endpointCode, String instanceId, CpfServiceRegistryControlPort.InstanceCommand command,
            String reason, String operator) {
        return controlPort.changeInstanceStateTyped(serviceId, endpointCode, instanceId, command, reason, operator);
    }
    public void deleteService(String id, CpfServiceRegistryControlPort.DeleteCommand command) { controlPort.deleteService(id, command); }
    public void deleteEndpoint(String id, CpfServiceRegistryControlPort.DeleteCommand command) { controlPort.deleteEndpoint(id, command); }
    public void deleteInstance(String id, CpfServiceRegistryControlPort.DeleteCommand command) { controlPort.deleteInstance(id, command); }
}
