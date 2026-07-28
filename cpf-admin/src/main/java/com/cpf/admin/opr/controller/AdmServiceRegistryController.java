package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmServiceRegistryService;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.core.api.servicecall.CpfServiceRegistryControlPort;
import com.cpf.core.api.logging.CpfTransactionContext;
import jakarta.servlet.http.HttpServletRequest;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/**
 * ADM 서비스 레지스트리 조회 API입니다.
 */
@RestController
@RequestMapping("/adm/api/service-registry")
@Tag(name = "ADM-ServiceRegistry", description = "CPF 서비스 호출 엔진 레지스트리 운영 조회 API")
public class AdmServiceRegistryController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmServiceRegistryService serviceRegistryService;
    private final AdmAuditLogService auditLogService;

    public AdmServiceRegistryController(AdmServiceRegistryService serviceRegistryService, AdmAuditLogService auditLogService) {
        this.serviceRegistryService = serviceRegistryService;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/services")
    @CpfOnlineTransaction(id = "OADMSV0010", name = "ADMServiceRegistryServices")
    @Operation(operationId = "admServiceRegistryFindServices", summary = "서비스 목록 조회", description = "CPF 서비스 호출 엔진에 등록된 서비스 기본 정보를 조회합니다.")
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
    @CpfOnlineTransaction(id = "OADMSV0020", name = "ADMServiceRegistryEndpoints")
    @Operation(operationId = "admServiceRegistryFindEndpoints", summary = "서비스 endpoint 조회", description = "서비스별 endpoint, base URL, timeout/retry 기본값을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findEndpoints(
            @RequestParam(required = false) String serviceId,
            @RequestParam(required = false) String endpointCode,
            @RequestParam(required = false) String useYn,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(serviceRegistryService.findEndpoints(serviceId, endpointCode, useYn, limit));
    }

    @GetMapping("/instances")
    @CpfOnlineTransaction(id = "OADMSV0030", name = "ADMServiceRegistryInstances")
    @Operation(operationId = "admServiceRegistryFindInstances", summary = "서비스 instance 조회", description = "endpoint별 instance와 현재 운영 상태를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findInstances(
            @RequestParam(required = false) String serviceId,
            @RequestParam(required = false) String endpointCode,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(serviceRegistryService.findInstances(serviceId, endpointCode, status, limit));
    }

    @GetMapping("/health")
    @CpfOnlineTransaction(id = "OADMSV0040", name = "ADMServiceRegistryHealth")
    @Operation(operationId = "admServiceRegistryFindHealth", summary = "서비스 health 조회", description = "서비스 instance health check 결과를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findHealth(
            @RequestParam(required = false) String serviceId,
            @RequestParam(required = false) String endpointCode,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(serviceRegistryService.findHealth(serviceId, endpointCode, limit));
    }

    @GetMapping("/routing-policies")
    @CpfOnlineTransaction(id = "OADMSV0050", name = "ADMServiceRegistryRoutingPolicies")
    @Operation(operationId = "admServiceRegistryFindRoutingPolicies", summary = "라우팅 정책 조회", description = "서비스 호출 엔진이 사용하는 routing/failover/health policy를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findRoutingPolicies(
            @RequestParam(required = false) String serviceId,
            @RequestParam(required = false) String endpointCode,
            @RequestParam(required = false) String activeYn,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(serviceRegistryService.findRoutingPolicies(serviceId, endpointCode, activeYn, limit));
    }

    @GetMapping("/circuit-states")
    @CpfOnlineTransaction(id = "OADMSV0060", name = "ADMServiceRegistryCircuitStates")
    @Operation(operationId = "admServiceRegistryFindCircuitStates", summary = "Circuit 상태 조회", description = "서비스/endpoint/instance별 circuit breaker 상태를 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findCircuitStates(
            @RequestParam(required = false) String serviceId,
            @RequestParam(required = false) String endpointCode,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(serviceRegistryService.findCircuitStates(serviceId, endpointCode, limit));
    }

    @GetMapping("/call-history")
    @CpfOnlineTransaction(id = "OADMSV0070", name = "ADMServiceRegistryCallHistory")
    @Operation(operationId = "admServiceRegistryFindCallHistory", summary = "서비스 호출 이력 조회", description = "CPF 서비스 호출 엔진이 기록한 호출 이력을 조회합니다.")
    public ResponseEntity<List<Map<String, Object>>> findCallHistory(
            @RequestParam(required = false) String serviceId,
            @RequestParam(required = false) String transactionId,
            @RequestParam(defaultValue = "100") int limit) {
        return ResponseEntity.ok(serviceRegistryService.findCallHistory(serviceId, transactionId, limit));
    }
    @PostMapping("/services")
    @CpfOnlineTransaction(id="OADMSV0080",name="ADMServiceRegistryServiceSave")
    @Operation(operationId="admServiceRegistrySaveService",summary="서비스 생성/수정")
    public ResponseEntity<Map<String,Object>> saveService(@RequestBody CpfServiceRegistryControlPort.ServiceDefinition command,HttpServletRequest request){String operator=operator(request);requireOwner(command.requestedBy(),operator);Map<String,Object> result=serviceRegistryService.saveService(command);audit(request,operator,"SERVICE_REGISTRY_SERVICE_SAVE",command.serviceId(),command.reason(),result);return ResponseEntity.ok(result);}

    @PostMapping("/endpoints")
    @CpfOnlineTransaction(id="OADMSV0090",name="ADMServiceRegistryEndpointSave")
    @Operation(operationId="admServiceRegistrySaveEndpoint",summary="Endpoint 생성/수정")
    public ResponseEntity<Map<String,Object>> saveEndpoint(@RequestBody CpfServiceRegistryControlPort.EndpointDefinition command,HttpServletRequest request){String operator=operator(request);requireOwner(command.requestedBy(),operator);Map<String,Object> result=serviceRegistryService.saveEndpoint(command);audit(request,operator,"SERVICE_REGISTRY_ENDPOINT_SAVE",command.endpointCode(),command.reason(),result);return ResponseEntity.ok(result);}

    @PostMapping("/instances")
    @CpfOnlineTransaction(id="OADMSV0100",name="ADMServiceRegistryInstanceSave")
    @Operation(operationId="admServiceRegistrySaveInstance",summary="Instance 생성/수정")
    public ResponseEntity<Map<String,Object>> saveInstance(@RequestBody CpfServiceRegistryControlPort.InstanceDefinition command,HttpServletRequest request){String operator=operator(request);requireOwner(command.requestedBy(),operator);Map<String,Object> result=serviceRegistryService.saveInstance(command);audit(request,operator,"SERVICE_REGISTRY_INSTANCE_SAVE",command.instanceId(),command.reason(),result);return ResponseEntity.ok(result);}

    @DeleteMapping("/services/{serviceId}") @CpfOnlineTransaction(id="OADMSV0110",name="ADMServiceRegistryServiceDelete")
    @Operation(operationId="admServiceRegistryDeleteService",summary="서비스 삭제")
    public ResponseEntity<Void> deleteService(@PathVariable String serviceId,@RequestBody CpfServiceRegistryControlPort.DeleteCommand command,HttpServletRequest request){String operator=operator(request);requireOwner(command.requestedBy(),operator);serviceRegistryService.deleteService(serviceId,command);audit(request,operator,"SERVICE_REGISTRY_SERVICE_DELETE",serviceId,command.reason(),Map.of("deleted",true));return ResponseEntity.noContent().build();}

    @DeleteMapping("/endpoints/{endpointCode}") @CpfOnlineTransaction(id="OADMSV0120",name="ADMServiceRegistryEndpointDelete")
    @Operation(operationId="admServiceRegistryDeleteEndpoint",summary="Endpoint 삭제")
    public ResponseEntity<Void> deleteEndpoint(@PathVariable String endpointCode,@RequestBody CpfServiceRegistryControlPort.DeleteCommand command,HttpServletRequest request){String operator=operator(request);requireOwner(command.requestedBy(),operator);serviceRegistryService.deleteEndpoint(endpointCode,command);audit(request,operator,"SERVICE_REGISTRY_ENDPOINT_DELETE",endpointCode,command.reason(),Map.of("deleted",true));return ResponseEntity.noContent().build();}

    @DeleteMapping("/instances/{instanceId}") @CpfOnlineTransaction(id="OADMSV0130",name="ADMServiceRegistryInstanceDelete")
    @Operation(operationId="admServiceRegistryDeleteInstance",summary="Instance 삭제")
    public ResponseEntity<Void> deleteInstance(@PathVariable String instanceId,@RequestBody CpfServiceRegistryControlPort.DeleteCommand command,HttpServletRequest request){String operator=operator(request);requireOwner(command.requestedBy(),operator);serviceRegistryService.deleteInstance(instanceId,command);audit(request,operator,"SERVICE_REGISTRY_INSTANCE_DELETE",instanceId,command.reason(),Map.of("deleted",true));return ResponseEntity.noContent().build();}

    private String operator(HttpServletRequest request){Object value=request.getAttribute("adm.operatorId");if(value instanceof String s&&!s.isBlank())return s;throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"검증된 ADM operator session이 필요합니다.");}
    private void requireOwner(String requestedBy,String operator){if(requestedBy==null||!operator.equals(requestedBy))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"requestedBy는 인증된 ADM 운영자와 일치해야 합니다.");}
    private void audit(HttpServletRequest req,String user,String action,String id,String reason,Object after){auditLogService.record(CpfTransactionContext.transactionId(),user,action,"cpf_service_registry",id,reason,"",String.valueOf(after),"Service Registry",req.getRemoteAddr());}

}
