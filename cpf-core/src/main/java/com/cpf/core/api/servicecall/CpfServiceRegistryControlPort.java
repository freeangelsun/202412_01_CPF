package com.cpf.core.api.servicecall;

import java.util.Map;

/**
 * Service Registry의 운영 상태를 변경하는 topology-independent command port입니다.
 * ADM은 이 Port만 소비하며 cpf_service_instance를 직접 변경하지 않습니다.
 */
public interface CpfServiceRegistryControlPort {
    Map<String,Object> changeInstanceState(String serviceId, String endpointCode, String instanceId, InstanceCommand command, String reason, String requestedBy);
    enum InstanceCommand { DRAIN, DISABLE, RESUME }
}
