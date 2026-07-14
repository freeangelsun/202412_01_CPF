package cpf.adm.opr.controller;

import cpf.adm.opr.service.AdmServiceRegistryService;
import cpf.pfw.common.logging.CpfTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * ADM 서비스 레지스트리 조회 API입니다.
 */
@RestController
@RequestMapping("/adm/api/service-registry")
@Tag(name = "ADM-ServiceRegistry", description = "PFW 서비스 호출 엔진 레지스트리 운영 조회 API")
public class AdmServiceRegistryController {
    private final AdmServiceRegistryService serviceRegistryService;

    public AdmServiceRegistryController(AdmServiceRegistryService serviceRegistryService) {
        this.serviceRegistryService = serviceRegistryService;
    }

    @GetMapping("/services")
    @CpfTransaction(id = "ADM01SVC0010", name = "ADMServiceRegistryServices")
    @Operation(operationId = "admServiceRegistryFindServices", summary = "서비스 목록 조회", description = "PFW 서비스 호출 엔진에 등록된 서비스 기본 정보를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findServices(
            @Parameter(description = "서비스 ID", example = "MBR")
            @RequestParam(required = false) String serviceId,
            @Parameter(description = "사용 여부", example = "Y")
            @RequestParam(required = false) String useYn,
            @Parameter(description = "조회 건수", example = "100")
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(serviceRegistryService.findServices(serviceId, useYn, limit));
    }

    @GetMapping("/endpoints")
    @CpfTransaction(id = "ADM01SVC0020", name = "ADMServiceRegistryEndpoints")
    @Operation(operationId = "admServiceRegistryFindEndpoints", summary = "서비스 endpoint 조회", description = "서비스별 endpoint, base URL, timeout/retry 기본값을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findEndpoints(
            @RequestParam(required = false) String serviceId,
            @RequestParam(required = false) String endpointCode,
            @RequestParam(required = false) String useYn,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(serviceRegistryService.findEndpoints(serviceId, endpointCode, useYn, limit));
    }

    @GetMapping("/instances")
    @CpfTransaction(id = "ADM01SVC0030", name = "ADMServiceRegistryInstances")
    @Operation(operationId = "admServiceRegistryFindInstances", summary = "서비스 instance 조회", description = "endpoint별 instance와 현재 운영 상태를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findInstances(
            @RequestParam(required = false) String serviceId,
            @RequestParam(required = false) String endpointCode,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(serviceRegistryService.findInstances(serviceId, endpointCode, status, limit));
    }

    @GetMapping("/health")
    @CpfTransaction(id = "ADM01SVC0040", name = "ADMServiceRegistryHealth")
    @Operation(operationId = "admServiceRegistryFindHealth", summary = "서비스 health 조회", description = "서비스 instance health check 결과를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findHealth(
            @RequestParam(required = false) String serviceId,
            @RequestParam(required = false) String endpointCode,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(serviceRegistryService.findHealth(serviceId, endpointCode, limit));
    }

    @GetMapping("/routing-policies")
    @CpfTransaction(id = "ADM01SVC0050", name = "ADMServiceRegistryRoutingPolicies")
    @Operation(operationId = "admServiceRegistryFindRoutingPolicies", summary = "라우팅 정책 조회", description = "서비스 호출 엔진이 사용하는 routing/failover/health policy를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findRoutingPolicies(
            @RequestParam(required = false) String serviceId,
            @RequestParam(required = false) String endpointCode,
            @RequestParam(required = false) String activeYn,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(serviceRegistryService.findRoutingPolicies(serviceId, endpointCode, activeYn, limit));
    }

    @GetMapping("/circuit-states")
    @CpfTransaction(id = "ADM01SVC0060", name = "ADMServiceRegistryCircuitStates")
    @Operation(operationId = "admServiceRegistryFindCircuitStates", summary = "Circuit 상태 조회", description = "서비스/endpoint/instance별 circuit breaker 상태를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findCircuitStates(
            @RequestParam(required = false) String serviceId,
            @RequestParam(required = false) String endpointCode,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(serviceRegistryService.findCircuitStates(serviceId, endpointCode, limit));
    }

    @GetMapping("/call-history")
    @CpfTransaction(id = "ADM01SVC0070", name = "ADMServiceRegistryCallHistory")
    @Operation(operationId = "admServiceRegistryFindCallHistory", summary = "서비스 호출 이력 조회", description = "PFW 서비스 호출 엔진이 기록한 호출 이력을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findCallHistory(
            @RequestParam(required = false) String serviceId,
            @RequestParam(required = false) String transactionGlobalId,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(serviceRegistryService.findCallHistory(serviceId, transactionGlobalId, limit));
    }
}
