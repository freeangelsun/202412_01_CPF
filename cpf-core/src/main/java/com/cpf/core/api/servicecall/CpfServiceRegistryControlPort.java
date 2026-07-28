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
