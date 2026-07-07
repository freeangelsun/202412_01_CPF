package cpf.pfw.common.servicecall;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * CPF 서비스 instance 정보를 조회하는 레지스트리입니다.
 */
public class CpfServiceInstanceRegistry {
    private final CpfServiceRegistryRepository repository;

    public CpfServiceInstanceRegistry(CpfServiceRegistryRepository repository) {
        this.repository = repository;
    }

    public List<Map<String, Object>> findInstances(String serviceId, String endpointCode, String status, int limit) {
        return repository.findInstances(serviceId, endpointCode, status, limit);
    }

    public Optional<Map<String, Object>> findInstance(String serviceId, String endpointCode, String instanceId) {
        return repository.findInstances(serviceId, endpointCode, null, 1000).stream()
                .filter(row -> instanceId != null && instanceId.equals(String.valueOf(row.get("instanceId"))))
                .findFirst();
    }
}
