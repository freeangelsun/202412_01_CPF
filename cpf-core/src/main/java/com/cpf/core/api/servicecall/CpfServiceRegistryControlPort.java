package com.cpf.core.api.servicecall;

import java.util.Locale;
import java.util.Objects;

/** Service Registry의 topology-independent Typed 운영 Command Port입니다. */
public interface CpfServiceRegistryControlPort {
    CpfServiceRegistryView.MutationResult saveService(ServiceDefinition command);
    CpfServiceRegistryView.MutationResult saveEndpoint(EndpointDefinition command);
    CpfServiceRegistryView.MutationResult saveInstance(InstanceDefinition command);
    CpfServiceRegistryView.MutationResult deleteService(String serviceId, DeleteCommand command);
    CpfServiceRegistryView.MutationResult deleteEndpoint(String endpointCode, DeleteCommand command);
    CpfServiceRegistryView.MutationResult deleteInstance(String instanceId, DeleteCommand command);
    CpfServiceRegistryView.MutationResult changeInstanceState(
            String serviceId, String endpointCode, String instanceId, InstanceStateCommand command);

    enum InstanceCommand { DRAIN, DISABLE, RESUME }

    record ServiceDefinition(
            String operationId, String serviceId, String serviceName, String serviceType,
            String ownerModuleCode, String description, String useYn, Long expectedVersion,
            String reason, String requestedBy) {
        public ServiceDefinition {
            operationId = required(operationId, "operationId");
            serviceId = required(serviceId, "serviceId");
            serviceName = required(serviceName, "serviceName");
            serviceType = CpfServiceRegistryCatalog.requireServiceType(serviceType);
            ownerModuleCode = required(ownerModuleCode, "ownerModuleCode");
            useYn = yn(useYn, "Y");
            reason = validatedReason(reason);
            requestedBy = required(requestedBy, "requestedBy");
        }
        public ServiceDefinition withActor(String actor) {
            return new ServiceDefinition(operationId, serviceId, serviceName, serviceType, ownerModuleCode,
                    description, useYn, expectedVersion, reason, actor);
        }
    }

    record EndpointDefinition(
            String operationId, String endpointCode, String serviceId, String endpointName,
            String endpointType, String baseUrl, String contextPath, Integer defaultTimeoutMs,
            Integer defaultRetryCount, String useYn, Long expectedVersion, String reason, String requestedBy) {
        public EndpointDefinition {
            operationId = required(operationId, "operationId");
            endpointCode = required(endpointCode, "endpointCode");
            serviceId = required(serviceId, "serviceId");
            endpointName = required(endpointName, "endpointName");
            endpointType = CpfServiceRegistryCatalog.requireEndpointType(endpointType);
            baseUrl = required(baseUrl, "baseUrl");
            if (defaultTimeoutMs != null && defaultTimeoutMs <= 0) {
                throw new IllegalArgumentException("defaultTimeoutMs must be positive");
            }
            if (defaultRetryCount != null && defaultRetryCount < 0) {
                throw new IllegalArgumentException("defaultRetryCount must be non-negative");
            }
            useYn = yn(useYn, "Y");
            reason = validatedReason(reason);
            requestedBy = required(requestedBy, "requestedBy");
        }
        public EndpointDefinition withActor(String actor) {
            return new EndpointDefinition(operationId, endpointCode, serviceId, endpointName, endpointType,
                    baseUrl, contextPath, defaultTimeoutMs, defaultRetryCount, useYn, expectedVersion, reason, actor);
        }
    }

    record InstanceDefinition(
            String operationId, String instanceId, String serviceId, String endpointCode,
            String instanceName, String baseUrl, String hostName, Integer portNo,
            String environmentCode, String zoneCode, String cellCode, Integer weight,
            Integer priorityNo, String activeYn, String maintenanceYn, String drainYn,
            Long expectedVersion, String reason, String requestedBy) {
        public InstanceDefinition {
            operationId = required(operationId, "operationId");
            instanceId = required(instanceId, "instanceId");
            serviceId = required(serviceId, "serviceId");
            endpointCode = required(endpointCode, "endpointCode");
            instanceName = required(instanceName, "instanceName");
            baseUrl = required(baseUrl, "baseUrl");
            if (portNo != null && (portNo <= 0 || portNo > 65535)) {
                throw new IllegalArgumentException("portNo out of range");
            }
            if (weight != null && weight <= 0) throw new IllegalArgumentException("weight must be positive");
            if (priorityNo != null && priorityNo <= 0) throw new IllegalArgumentException("priorityNo must be positive");
            environmentCode = CpfServiceRegistryCatalog.requireEnvironment(environmentCode);
            activeYn = yn(activeYn, "Y");
            maintenanceYn = yn(maintenanceYn, "N");
            drainYn = yn(drainYn, "N");
            reason = validatedReason(reason);
            requestedBy = required(requestedBy, "requestedBy");
        }
        public InstanceDefinition withActor(String actor) {
            return new InstanceDefinition(operationId, instanceId, serviceId, endpointCode, instanceName,
                    baseUrl, hostName, portNo, environmentCode, zoneCode, cellCode, weight, priorityNo,
                    activeYn, maintenanceYn, drainYn, expectedVersion, reason, actor);
        }
    }

    record DeleteCommand(String operationId, Long expectedVersion, String reason, String requestedBy) {
        public DeleteCommand {
            operationId = required(operationId, "operationId");
            if (expectedVersion == null || expectedVersion < 0) {
                throw new IllegalArgumentException("expectedVersion is required");
            }
            reason = validatedReason(reason);
            requestedBy = required(requestedBy, "requestedBy");
        }
        public DeleteCommand withActor(String actor) {
            return new DeleteCommand(operationId, expectedVersion, reason, actor);
        }
    }

    record InstanceStateCommand(
            String operationId, InstanceCommand command, Long expectedVersion,
            String reason, String requestedBy) {
        public InstanceStateCommand {
            operationId = required(operationId, "operationId");
            command = Objects.requireNonNull(command, "command");
            if (expectedVersion == null || expectedVersion < 0) {
                throw new IllegalArgumentException("expectedVersion is required");
            }
            reason = validatedReason(reason);
            requestedBy = required(requestedBy, "requestedBy");
        }
        public InstanceStateCommand withActor(String actor) {
            return new InstanceStateCommand(operationId, command, expectedVersion, reason, actor);
        }
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }
    private static String validatedReason(String value) {
        String resolved = required(value, "reason");
        if (resolved.length() < 5) throw new IllegalArgumentException("reason must be at least 5 characters");
        return resolved;
    }
    private static String yn(String value, String fallback) {
        String resolved = value == null || value.isBlank()
                ? fallback : value.trim().toUpperCase(Locale.ROOT);
        if (!"Y".equals(resolved) && !"N".equals(resolved)) {
            throw new IllegalArgumentException("Y/N code required");
        }
        return resolved;
    }
    private static String code(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value.trim().toUpperCase(Locale.ROOT);
    }
}
