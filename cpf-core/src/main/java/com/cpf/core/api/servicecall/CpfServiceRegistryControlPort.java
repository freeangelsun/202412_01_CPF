package com.cpf.core.api.servicecall;

import java.util.Locale;
import java.util.Objects;

/** Service Registry의 topology-independent Typed 운영 Command Port입니다. */
public interface CpfServiceRegistryControlPort {
    /** 서비스 정의를 생성 또는 낙관적 version 기준으로 변경합니다. @param command 서비스 변경 명령 @return 저장 결과 */
    CpfServiceRegistryView.MutationResult saveService(ServiceDefinition command);
    /** Endpoint 정의를 생성 또는 변경합니다. @param command Endpoint 변경 명령 @return 저장 결과 */
    CpfServiceRegistryView.MutationResult saveEndpoint(EndpointDefinition command);
    /** Service Instance 정의를 생성 또는 변경합니다. @param command Instance 변경 명령 @return 저장 결과 */
    CpfServiceRegistryView.MutationResult saveInstance(InstanceDefinition command);
    /** 서비스를 낙관적 version과 감사 사유를 확인한 뒤 삭제합니다. @param serviceId 서비스 식별자 @param command 삭제 명령 @return 삭제 결과 */
    CpfServiceRegistryView.MutationResult deleteService(String serviceId, DeleteCommand command);
    /** Endpoint를 낙관적 version과 감사 사유를 확인한 뒤 삭제합니다. @param endpointCode Endpoint 코드 @param command 삭제 명령 @return 삭제 결과 */
    CpfServiceRegistryView.MutationResult deleteEndpoint(String endpointCode, DeleteCommand command);
    /** Instance를 낙관적 version과 감사 사유를 확인한 뒤 삭제합니다. @param instanceId Instance 식별자 @param command 삭제 명령 @return 삭제 결과 */
    CpfServiceRegistryView.MutationResult deleteInstance(String instanceId, DeleteCommand command);
    /**
     * Instance의 drain/disable/resume 운영 상태를 변경합니다.
     * @param serviceId 서비스 식별자
     * @param endpointCode Endpoint 코드
     * @param instanceId Instance 식별자
     * @param command 상태 변경 명령
     * @return 상태 변경 결과
     */
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
        /** 인증 경계에서 파생한 실행 주체를 주입한 새 명령을 만듭니다. @param actor 인증된 실행 주체 @return actor가 교체된 서비스 명령 */
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
        /** 인증 경계에서 파생한 실행 주체를 주입한 새 명령을 만듭니다. @param actor 인증된 실행 주체 @return actor가 교체된 Endpoint 명령 */
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
        /** 인증 경계에서 파생한 실행 주체를 주입한 새 명령을 만듭니다. @param actor 인증된 실행 주체 @return actor가 교체된 Instance 명령 */
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
        /** 인증 경계에서 파생한 실행 주체를 주입한 새 삭제 명령을 만듭니다. @param actor 인증된 실행 주체 @return actor가 교체된 삭제 명령 */
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
        /** 인증 경계에서 파생한 실행 주체를 주입한 새 상태 명령을 만듭니다. @param actor 인증된 실행 주체 @return actor가 교체된 상태 명령 */
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
