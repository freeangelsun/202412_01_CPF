package com.cpf.admin.opr.controller;

import org.springframework.web.bind.annotation.RestController;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmServiceRegistryService;
import com.cpf.data.api.CpfDataRow;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.integration.api.servicecall.CpfServiceRegistryControlPort;
import com.cpf.integration.api.servicecall.CpfServiceRegistryCatalog;
import com.cpf.integration.api.servicecall.CpfServiceRegistryView;
import io.swagger.v3.oas.annotations.Hidden;
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

    @GetMapping("/capabilities")
    @Operation(operationId="admServiceRegistryCapabilities",summary="Service Registry Code·Capability 조회")
    public CpfDataRow capabilities() {
        return CpfDataRow.of(
                "catalogVersion","1",
                "serviceTypes",CpfServiceRegistryCatalog.SERVICE_TYPES,
                "endpointTypes",CpfServiceRegistryCatalog.ENDPOINT_TYPES,
                "instanceStatuses",CpfServiceRegistryCatalog.INSTANCE_STATUSES,
                "instanceCommands",java.util.Arrays.stream(CpfServiceRegistryControlPort.InstanceCommand.values()).map(Enum::name).toList(),
                "environments",CpfServiceRegistryCatalog.ENVIRONMENTS);
    }

    @GetMapping("/services")    @Operation(operationId="admServiceRegistryFindServices",summary="서비스 목록 조회")
    public ResponseEntity<List<CpfServiceRegistryView.Service>> findServices(
            @Parameter(description="서비스 ID",example="MBR") @RequestParam(required=false) String serviceId,
            @RequestParam(required=false) String useYn,@RequestParam(defaultValue="100") int limit) {
        return ResponseEntity.ok(service.findServices(serviceId,useYn,limit));
    }

    @GetMapping("/endpoints")    @Operation(operationId="admServiceRegistryFindEndpoints",summary="Endpoint 목록 조회")
    public ResponseEntity<List<CpfServiceRegistryView.Endpoint>> findEndpoints(
            @RequestParam(required=false) String serviceId,@RequestParam(required=false) String endpointCode,
            @RequestParam(required=false) String useYn,@RequestParam(defaultValue="100") int limit) {
        return ResponseEntity.ok(service.findEndpoints(serviceId,endpointCode,useYn,limit));
    }

    @GetMapping("/instances")    @Operation(operationId="admServiceRegistryFindInstances",summary="Instance 목록 조회")
    public ResponseEntity<List<CpfServiceRegistryView.Instance>> findInstances(
            @RequestParam(required=false) String serviceId,@RequestParam(required=false) String endpointCode,
            @RequestParam(required=false) String status,@RequestParam(defaultValue="100") int limit) {
        return ResponseEntity.ok(service.findInstances(serviceId,endpointCode,status,limit));
    }

    @GetMapping("/health")    @Operation(operationId="admServiceRegistryFindHealth",summary="Instance Health 조회")
    public ResponseEntity<List<CpfServiceRegistryView.Health>> findHealth(
            @RequestParam(required=false) String serviceId,@RequestParam(required=false) String endpointCode,
            @RequestParam(defaultValue="100") int limit) { return ResponseEntity.ok(service.findHealth(serviceId,endpointCode,limit)); }

    @GetMapping("/routing-policies")    @Operation(operationId="admServiceRegistryFindRoutingPolicies",summary="라우팅 정책 조회")
    public ResponseEntity<List<CpfServiceRegistryView.RoutingPolicy>> findRoutingPolicies(
            @RequestParam(required=false) String serviceId,@RequestParam(required=false) String endpointCode,
            @RequestParam(required=false) String activeYn,@RequestParam(defaultValue="100") int limit) {
        return ResponseEntity.ok(service.findRoutingPolicies(serviceId,endpointCode,activeYn,limit));
    }

    @GetMapping("/circuit-states")    @Operation(operationId="admServiceRegistryFindCircuitStates",summary="Circuit 상태 조회")
    public ResponseEntity<List<CpfServiceRegistryView.CircuitState>> findCircuitStates(
            @RequestParam(required=false) String serviceId,@RequestParam(required=false) String endpointCode,
            @RequestParam(defaultValue="100") int limit) { return ResponseEntity.ok(service.findCircuitStates(serviceId,endpointCode,limit)); }

    @GetMapping("/call-history")    @Operation(operationId="admServiceRegistryFindCallHistory",summary="서비스 호출 이력 조회")
    public ResponseEntity<List<CpfServiceRegistryView.CallHistory>> findCallHistory(
            @RequestParam(required=false) String serviceId,@RequestParam(required=false) String transactionId,
            @RequestParam(defaultValue="100") int limit) { return ResponseEntity.ok(service.findCallHistory(serviceId,transactionId,limit)); }

    @PostMapping("/services")    @Operation(operationId="admServiceRegistrySaveService",summary="서비스 생성·수정")
    public ResponseEntity<CpfServiceRegistryView.MutationResult> saveService(
            @RequestBody CpfServiceRegistryControlPort.ServiceDefinition command,HttpServletRequest request) {
        String operator=operator(request);
        var secured=command.withActor(operator);
        var result=service.saveService(secured);
        audit(request,operator,"SERVICE_REGISTRY_SERVICE_SAVE",secured.serviceId(),secured.reason(),result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/endpoints")    @Operation(operationId="admServiceRegistrySaveEndpoint",summary="Endpoint 생성·수정")
    public ResponseEntity<CpfServiceRegistryView.MutationResult> saveEndpoint(
            @RequestBody CpfServiceRegistryControlPort.EndpointDefinition command,HttpServletRequest request) {
        String operator=operator(request);
        var secured=command.withActor(operator);
        var result=service.saveEndpoint(secured);
        audit(request,operator,"SERVICE_REGISTRY_ENDPOINT_SAVE",secured.endpointCode(),secured.reason(),result);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/instances")    @Operation(operationId="admServiceRegistrySaveInstance",summary="Instance 생성·수정")
    public ResponseEntity<CpfServiceRegistryView.MutationResult> saveInstance(
            @RequestBody CpfServiceRegistryControlPort.InstanceDefinition command,HttpServletRequest request) {
        String operator=operator(request);
        var secured=command.withActor(operator);
        var result=service.saveInstance(secured);
        audit(request,operator,"SERVICE_REGISTRY_INSTANCE_SAVE",secured.instanceId(),secured.reason(),result);
        return ResponseEntity.ok(result);
    }

    /** 직접 상태 변경 HTTP 경로는 Approval Owner Command 우회를 막기 위해 폐기했습니다. */
    @PostMapping("/services/{serviceId}/endpoints/{endpointCode}/instances/{instanceId}/state")
    @Hidden
    public ResponseEntity<Void> changeInstanceState(
            @PathVariable String serviceId,@PathVariable String endpointCode,@PathVariable String instanceId,
            @RequestBody InstanceStateRequest body,HttpServletRequest request) {
        throw retiredDangerousCommand();
    }

    /** 직접 삭제 HTTP 경로는 승인 Snapshot·SoD·감사 실행을 강제하기 위해 폐기했습니다. */
    @DeleteMapping("/services/{serviceId}")
    @Hidden
    public ResponseEntity<Void> deleteService(@PathVariable String serviceId,@RequestBody CpfServiceRegistryControlPort.DeleteCommand command,HttpServletRequest request) {
        throw retiredDangerousCommand();
    }
    @DeleteMapping("/endpoints/{endpointCode}")
    @Hidden
    public ResponseEntity<Void> deleteEndpoint(@PathVariable String endpointCode,@RequestBody CpfServiceRegistryControlPort.DeleteCommand command,HttpServletRequest request) {
        throw retiredDangerousCommand();
    }
    @DeleteMapping("/instances/{instanceId}")
    @Hidden
    public ResponseEntity<Void> deleteInstance(@PathVariable String instanceId,@RequestBody CpfServiceRegistryControlPort.DeleteCommand command,HttpServletRequest request) {
        throw retiredDangerousCommand();
    }

    private ResponseStatusException retiredDangerousCommand() {
        return new ResponseStatusException(HttpStatus.GONE,
                "Service Registry 위험조치는 Approval 요청 → 독립 승인 → Owner Command 경로로만 실행할 수 있습니다.");
    }

    private String operator(HttpServletRequest request) {
        Object value=request.getAttribute("adm.operatorId");
        if(value instanceof String s&&!s.isBlank()) return s;
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"검증된 ADM operator session이 필요합니다.");
    }
    private void requireReason(String reason) {
        if(reason==null||reason.trim().length()<5) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"운영 조치 사유는 5자 이상이어야 합니다.");
    }
    private void audit(HttpServletRequest req,String user,String action,String id,String reason,Object after) {
        auditLogService.record(CpfContexts.transactionId(),user,action,"cpf_service_registry",id,reason,"",String.valueOf(after),"Service Registry",req.getRemoteAddr());
    }

    public record InstanceStateRequest(String operationId,CpfServiceRegistryControlPort.InstanceCommand command,Long expectedVersion,String reason) {}
}
