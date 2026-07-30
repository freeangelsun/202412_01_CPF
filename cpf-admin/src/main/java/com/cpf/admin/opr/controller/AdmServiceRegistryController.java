package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmServiceRegistryService;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.servicecall.CpfServiceRegistryControlPort;
import com.cpf.core.api.servicecall.CpfServiceRegistryView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

/** Service/Endpoint/Instance를 Typed API로 운영하는 ADM Controller입니다. */
@RestController
@RequestMapping("/adm/api/service-registry")
@Tag(name = "ADM-ServiceRegistry", description = "공통 Service Registry 조회·등록·상태 제어 API")
public class AdmServiceRegistryController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmServiceRegistryService service;
    private final AdmAuditLogService auditLogService;

    public AdmServiceRegistryController(AdmServiceRegistryService service, AdmAuditLogService auditLogService) {
        this.service = service;
        this.auditLogService = auditLogService;
    }

    @GetMapping("/services") @CpfOnlineTransaction(id="OADMSV0010",name="ADMServiceRegistryServices")
    @Operation(operationId="admServiceRegistryFindServices",summary="서비스 목록 조회")
    public ResponseEntity<List<CpfServiceRegistryView.Service>> findServices(
            @Parameter(description="서비스 ID",example="MBR") @RequestParam(required=false) String serviceId,
            @RequestParam(required=false) String useYn,@RequestParam(defaultValue="100") int limit) {
        return ResponseEntity.ok(service.findServices(serviceId,useYn,limit));
    }

    @GetMapping("/endpoints") @CpfOnlineTransaction(id="OADMSV0020",name="ADMServiceRegistryEndpoints")
    @Operation(operationId="admServiceRegistryFindEndpoints",summary="Endpoint 목록 조회")
    public ResponseEntity<List<CpfServiceRegistryView.Endpoint>> findEndpoints(
            @RequestParam(required=false) String serviceId,@RequestParam(required=false) String endpointCode,
            @RequestParam(required=false) String useYn,@RequestParam(defaultValue="100") int limit) {
        return ResponseEntity.ok(service.findEndpoints(serviceId,endpointCode,useYn,limit));
    }

    @GetMapping("/instances") @CpfOnlineTransaction(id="OADMSV0030",name="ADMServiceRegistryInstances")
    @Operation(operationId="admServiceRegistryFindInstances",summary="Instance 목록 조회")
    public ResponseEntity<List<CpfServiceRegistryView.Instance>> findInstances(
            @RequestParam(required=false) String serviceId,@RequestParam(required=false) String endpointCode,
            @RequestParam(required=false) String status,@RequestParam(defaultValue="100") int limit) {
        return ResponseEntity.ok(service.findInstances(serviceId,endpointCode,status,limit));
    }

    @GetMapping("/health") @CpfOnlineTransaction(id="OADMSV0040",name="ADMServiceRegistryHealth")
    @Operation(operationId="admServiceRegistryFindHealth",summary="Instance Health 조회")
    public ResponseEntity<List<CpfServiceRegistryView.Health>> findHealth(
            @RequestParam(required=false) String serviceId,@RequestParam(required=false) String endpointCode,
            @RequestParam(defaultValue="100") int limit) { return ResponseEntity.ok(service.findHealth(serviceId,endpointCode,limit)); }

    @GetMapping("/routing-policies") @CpfOnlineTransaction(id="OADMSV0050",name="ADMServiceRegistryRoutingPolicies")
    @Operation(operationId="admServiceRegistryFindRoutingPolicies",summary="라우팅 정책 조회")
    public ResponseEntity<List<CpfServiceRegistryView.RoutingPolicy>> findRoutingPolicies(
            @RequestParam(required=false) String serviceId,@RequestParam(required=false) String endpointCode,
            @RequestParam(required=false) String activeYn,@RequestParam(defaultValue="100") int limit) {
        return ResponseEntity.ok(service.findRoutingPolicies(serviceId,endpointCode,activeYn,limit));
    }

    @GetMapping("/circuit-states") @CpfOnlineTransaction(id="OADMSV0060",name="ADMServiceRegistryCircuitStates")
    @Operation(operationId="admServiceRegistryFindCircuitStates",summary="Circuit 상태 조회")
    public ResponseEntity<List<CpfServiceRegistryView.CircuitState>> findCircuitStates(
            @RequestParam(required=false) String serviceId,@RequestParam(required=false) String endpointCode,
            @RequestParam(defaultValue="100") int limit) { return ResponseEntity.ok(service.findCircuitStates(serviceId,endpointCode,limit)); }

    @GetMapping("/call-history") @CpfOnlineTransaction(id="OADMSV0070",name="ADMServiceRegistryCallHistory")
    @Operation(operationId="admServiceRegistryFindCallHistory",summary="서비스 호출 이력 조회")
    public ResponseEntity<List<CpfServiceRegistryView.CallHistory>> findCallHistory(
            @RequestParam(required=false) String serviceId,@RequestParam(required=false) String transactionId,
            @RequestParam(defaultValue="100") int limit) { return ResponseEntity.ok(service.findCallHistory(serviceId,transactionId,limit)); }

    @PostMapping("/services") @CpfOnlineTransaction(id="OADMSV0080",name="ADMServiceRegistryServiceSave")
    @Operation(operationId="admServiceRegistrySaveService",summary="서비스 생성·수정")
    public ResponseEntity<CpfServiceRegistryView.MutationResult> saveService(
            @RequestBody CpfServiceRegistryControlPort.ServiceDefinition command,HttpServletRequest request) {
        String operator=operator(request); requireOwner(command.requestedBy(),operator);
        var result=service.saveService(command); audit(request,operator,"SERVICE_REGISTRY_SERVICE_SAVE",command.serviceId(),command.reason(),result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/endpoints") @CpfOnlineTransaction(id="OADMSV0090",name="ADMServiceRegistryEndpointSave")
    @Operation(operationId="admServiceRegistrySaveEndpoint",summary="Endpoint 생성·수정")
    public ResponseEntity<CpfServiceRegistryView.MutationResult> saveEndpoint(
            @RequestBody CpfServiceRegistryControlPort.EndpointDefinition command,HttpServletRequest request) {
        String operator=operator(request); requireOwner(command.requestedBy(),operator);
        var result=service.saveEndpoint(command); audit(request,operator,"SERVICE_REGISTRY_ENDPOINT_SAVE",command.endpointCode(),command.reason(),result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/instances") @CpfOnlineTransaction(id="OADMSV0100",name="ADMServiceRegistryInstanceSave")
    @Operation(operationId="admServiceRegistrySaveInstance",summary="Instance 생성·수정")
    public ResponseEntity<CpfServiceRegistryView.MutationResult> saveInstance(
            @RequestBody CpfServiceRegistryControlPort.InstanceDefinition command,HttpServletRequest request) {
        String operator=operator(request); requireOwner(command.requestedBy(),operator);
        var result=service.saveInstance(command); audit(request,operator,"SERVICE_REGISTRY_INSTANCE_SAVE",command.instanceId(),command.reason(),result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/services/{serviceId}/endpoints/{endpointCode}/instances/{instanceId}/state")
    @CpfOnlineTransaction(id="OADMSV0105",name="ADMServiceRegistryInstanceState")
    @Operation(operationId="admServiceRegistryChangeInstanceState",summary="Instance Drain·Disable·Resume")
    public ResponseEntity<CpfServiceRegistryView.MutationResult> changeInstanceState(
            @PathVariable String serviceId,@PathVariable String endpointCode,@PathVariable String instanceId,
            @RequestBody InstanceStateRequest body,HttpServletRequest request) {
        String operator=operator(request); requireReason(body.reason());
        var result=service.changeInstanceState(serviceId,endpointCode,instanceId,body.command(),body.reason(),operator);
        audit(request,operator,"SERVICE_REGISTRY_INSTANCE_"+body.command(),instanceId,body.reason(),result);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/services/{serviceId}") @CpfOnlineTransaction(id="OADMSV0110",name="ADMServiceRegistryServiceDelete")
    @Operation(operationId="admServiceRegistryDeleteService",summary="서비스 삭제")
    public ResponseEntity<Void> deleteService(@PathVariable String serviceId,@RequestBody CpfServiceRegistryControlPort.DeleteCommand command,HttpServletRequest request) {
        String operator=operator(request);requireOwner(command.requestedBy(),operator);requireReason(command.reason());
        service.deleteService(serviceId,command);audit(request,operator,"SERVICE_REGISTRY_SERVICE_DELETE",serviceId,command.reason(),Map.of("deleted",true));return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/endpoints/{endpointCode}") @CpfOnlineTransaction(id="OADMSV0120",name="ADMServiceRegistryEndpointDelete")
    @Operation(operationId="admServiceRegistryDeleteEndpoint",summary="Endpoint 삭제")
    public ResponseEntity<Void> deleteEndpoint(@PathVariable String endpointCode,@RequestBody CpfServiceRegistryControlPort.DeleteCommand command,HttpServletRequest request) {
        String operator=operator(request);requireOwner(command.requestedBy(),operator);requireReason(command.reason());
        service.deleteEndpoint(endpointCode,command);audit(request,operator,"SERVICE_REGISTRY_ENDPOINT_DELETE",endpointCode,command.reason(),Map.of("deleted",true));return ResponseEntity.noContent().build();
    }
    @DeleteMapping("/instances/{instanceId}") @CpfOnlineTransaction(id="OADMSV0130",name="ADMServiceRegistryInstanceDelete")
    @Operation(operationId="admServiceRegistryDeleteInstance",summary="Instance 삭제")
    public ResponseEntity<Void> deleteInstance(@PathVariable String instanceId,@RequestBody CpfServiceRegistryControlPort.DeleteCommand command,HttpServletRequest request) {
        String operator=operator(request);requireOwner(command.requestedBy(),operator);requireReason(command.reason());
        service.deleteInstance(instanceId,command);audit(request,operator,"SERVICE_REGISTRY_INSTANCE_DELETE",instanceId,command.reason(),Map.of("deleted",true));return ResponseEntity.noContent().build();
    }

    private String operator(HttpServletRequest request) {
        Object value=request.getAttribute("adm.operatorId");
        if(value instanceof String s&&!s.isBlank()) return s;
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"검증된 ADM operator session이 필요합니다.");
    }
    private void requireOwner(String requestedBy,String operator) {
        if(requestedBy==null||!operator.equals(requestedBy)) throw new ResponseStatusException(HttpStatus.FORBIDDEN,"requestedBy는 인증된 ADM 운영자와 일치해야 합니다.");
    }
    private void requireReason(String reason) {
        if(reason==null||reason.trim().length()<5) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"운영 조치 사유는 5자 이상이어야 합니다.");
    }
    private void audit(HttpServletRequest req,String user,String action,String id,String reason,Object after) {
        auditLogService.record(CpfTransactionContext.transactionId(),user,action,"cpf_service_registry",id,reason,"",String.valueOf(after),"Service Registry",req.getRemoteAddr());
    }

    public record InstanceStateRequest(CpfServiceRegistryControlPort.InstanceCommand command,String reason) {}
}
