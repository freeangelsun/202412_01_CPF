package com.cpf.admin.opr.service;

import com.cpf.core.api.servicecall.CpfServiceRegistryQueryPort;
import com.cpf.core.api.servicecall.CpfServiceRegistryControlPort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * ADM 서비스 레지스트리 운영 조회 어댑터입니다.
 *
 * <p>ADM은 CPF 테이블이나 저장소를 직접 조회하지 않고 공개 Query Port만 사용합니다.</p>
 */
@Service
public class AdmServiceRegistryService extends com.cpf.admin.common.base.AdmBaseService {
    private final CpfServiceRegistryQueryPort queryPort;
    private final CpfServiceRegistryControlPort controlPort;

    public AdmServiceRegistryService(CpfServiceRegistryQueryPort queryPort, CpfServiceRegistryControlPort controlPort) {
        this.queryPort = queryPort;
        this.controlPort = controlPort;
    }

    public List<Map<String, Object>> findServices(String serviceId, String useYn, int limit) {
        return queryPort.findServices(serviceId, useYn, limit);
    }

    public List<Map<String, Object>> findEndpoints(String serviceId, String endpointCode, String useYn, int limit) {
        return queryPort.findEndpoints(serviceId, endpointCode, useYn, limit);
    }

    public List<Map<String, Object>> findInstances(String serviceId, String endpointCode, String status, int limit) {
        return queryPort.findInstances(serviceId, endpointCode, status, limit);
    }

    public List<Map<String, Object>> findHealth(String serviceId, String endpointCode, int limit) {
        return queryPort.findHealth(serviceId, endpointCode, limit);
    }

    public List<Map<String, Object>> findRoutingPolicies(String serviceId, String endpointCode, String activeYn, int limit) {
        return queryPort.findRoutingPolicies(serviceId, endpointCode, activeYn, limit);
    }

    public List<Map<String, Object>> findCircuitStates(String serviceId, String endpointCode, int limit) {
        return queryPort.findCircuitStates(serviceId, endpointCode, limit);
    }

    public List<Map<String, Object>> findCallHistory(String serviceId, String transactionId, int limit) {
        return queryPort.findCallHistory(serviceId, transactionId, limit);
    }
    public Map<String,Object> saveService(CpfServiceRegistryControlPort.ServiceDefinition command){return controlPort.saveService(command);}
    public Map<String,Object> saveEndpoint(CpfServiceRegistryControlPort.EndpointDefinition command){return controlPort.saveEndpoint(command);}
    public Map<String,Object> saveInstance(CpfServiceRegistryControlPort.InstanceDefinition command){return controlPort.saveInstance(command);}
    public void deleteService(String id,CpfServiceRegistryControlPort.DeleteCommand command){controlPort.deleteService(id,command);}
    public void deleteEndpoint(String id,CpfServiceRegistryControlPort.DeleteCommand command){controlPort.deleteEndpoint(id,command);}
    public void deleteInstance(String id,CpfServiceRegistryControlPort.DeleteCommand command){controlPort.deleteInstance(id,command);}

}
