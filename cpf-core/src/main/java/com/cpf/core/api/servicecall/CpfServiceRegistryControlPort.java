package com.cpf.core.api.servicecall;

import java.util.Map;

/** Service Registry의 topology-independent 운영 Command Port입니다. */
public interface CpfServiceRegistryControlPort {
    Map<String,Object> saveService(ServiceDefinition command);
    Map<String,Object> saveEndpoint(EndpointDefinition command);
    Map<String,Object> saveInstance(InstanceDefinition command);
    void deleteService(String serviceId, DeleteCommand command);
    void deleteEndpoint(String endpointCode, DeleteCommand command);
    void deleteInstance(String instanceId, DeleteCommand command);
    Map<String,Object> changeInstanceState(String serviceId, String endpointCode, String instanceId, InstanceCommand command, String reason, String requestedBy);

    default CpfServiceRegistryView.MutationResult saveServiceTyped(ServiceDefinition command) {
        return CpfServiceRegistryView.MutationResult.from("SERVICE", command.serviceId(), saveService(command));
    }
    default CpfServiceRegistryView.MutationResult saveEndpointTyped(EndpointDefinition command) {
        return CpfServiceRegistryView.MutationResult.from("ENDPOINT", command.endpointCode(), saveEndpoint(command));
    }
    default CpfServiceRegistryView.MutationResult saveInstanceTyped(InstanceDefinition command) {
        return CpfServiceRegistryView.MutationResult.from("INSTANCE", command.instanceId(), saveInstance(command));
    }
    default CpfServiceRegistryView.MutationResult changeInstanceStateTyped(
            String serviceId, String endpointCode, String instanceId, InstanceCommand command, String reason, String requestedBy) {
        return CpfServiceRegistryView.MutationResult.from("INSTANCE", instanceId,
                changeInstanceState(serviceId, endpointCode, instanceId, command, reason, requestedBy));
    }

    enum InstanceCommand { DRAIN, DISABLE, RESUME }

    record ServiceDefinition(String operationId,String serviceId,String serviceName,String serviceType,String ownerModuleCode,
                             String description,String useYn,Long expectedVersion,String reason,String requestedBy) {}
    record EndpointDefinition(String operationId,String endpointCode,String serviceId,String endpointName,String endpointType,
                              String baseUrl,String contextPath,Integer defaultTimeoutMs,Integer defaultRetryCount,String useYn,
                              Long expectedVersion,String reason,String requestedBy) {}
    record InstanceDefinition(String operationId,String instanceId,String serviceId,String endpointCode,String instanceName,
                              String baseUrl,String hostName,Integer portNo,String environmentCode,String zoneCode,String cellCode,
                              Integer weight,Integer priorityNo,String activeYn,String maintenanceYn,String drainYn,
                              Long expectedVersion,String reason,String requestedBy) {}
    record DeleteCommand(String operationId,Long expectedVersion,String reason,String requestedBy) {}
}
