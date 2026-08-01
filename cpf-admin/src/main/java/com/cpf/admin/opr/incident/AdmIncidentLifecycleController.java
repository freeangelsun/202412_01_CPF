package com.cpf.admin.opr.incident;

import com.cpf.core.api.execution.CpfOnlineTransaction;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static com.cpf.admin.opr.incident.AdmIncidentContracts.*;

/** 알림 Threshold부터 Incident·Escalation·Maintenance·Audit까지 제공하는 ADM 운영 API입니다. */
@RestController
@RequestMapping("/adm/api/incidents")
@Tag(name = "ADM-Incident", description = "Notification Incident lifecycle workbench")
public class AdmIncidentLifecycleController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmIncidentLifecycleService service;

    public AdmIncidentLifecycleController(AdmIncidentLifecycleService service) { this.service = service; }

    @GetMapping("/policies")
    @CpfOnlineTransaction(id="OADMIC0010",name="ADMIncidentPolicyPage")
    @Operation(operationId="admIncidentFindPolicies",summary="Incident 정책 Page 조회")
    public ResponseEntity<Page<PolicyResponse>> findPolicies(
            @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="50") int size,
            HttpServletRequest request){operator(request);return ResponseEntity.ok(service.findPolicies(page,size));}

    @PostMapping("/policies")
    @CpfOnlineTransaction(id="OADMIC0011",name="ADMIncidentPolicyCreate")
    @Operation(operationId="admIncidentCreatePolicy",summary="Incident 정책 등록")
    public ResponseEntity<PolicyResponse> createPolicy(@RequestBody PolicySaveRequest body,HttpServletRequest request){
        return ResponseEntity.ok(service.savePolicy(null,body,operator(request),request.getRemoteAddr()));}

    @PutMapping("/policies/{policyId}")
    @CpfOnlineTransaction(id="OADMIC0012",name="ADMIncidentPolicyUpdate")
    @Operation(operationId="admIncidentUpdatePolicy",summary="Incident 정책 수정")
    public ResponseEntity<PolicyResponse> updatePolicy(@PathVariable long policyId,@RequestBody PolicySaveRequest body,HttpServletRequest request){
        return ResponseEntity.ok(service.savePolicy(policyId,body,operator(request),request.getRemoteAddr()));}

    @PostMapping("/signals")
    @CpfOnlineTransaction(id="OADMIC0013",name="ADMIncidentSignalIngest")
    @Operation(operationId="admIncidentIngestSignal",summary="운영 Signal 수집 및 Threshold 평가")
    public ResponseEntity<SignalResult> ingestSignal(@RequestBody SignalRequest body,HttpServletRequest request){
        return ResponseEntity.ok(service.ingestSignal(body,operator(request)));}

    @GetMapping
    @CpfOnlineTransaction(id="OADMIC0020",name="ADMIncidentPage")
    @Operation(operationId="admIncidentFindIncidents",summary="Incident Page 조회")
    public ResponseEntity<Page<IncidentResponse>> findIncidents(
            @RequestParam(required=false) String status,@RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="50") int size,HttpServletRequest request){
        operator(request);return ResponseEntity.ok(service.findIncidents(status,page,size));}

    @GetMapping("/{incidentId}")
    @CpfOnlineTransaction(id="OADMIC0021",name="ADMIncidentDetail")
    @Operation(operationId="admIncidentFindIncident",summary="Incident 상세 조회")
    public ResponseEntity<IncidentResponse> findIncident(@PathVariable long incidentId,HttpServletRequest request){
        operator(request);return ResponseEntity.ok(service.findIncident(incidentId));}

    @GetMapping("/{incidentId}/timeline")
    @CpfOnlineTransaction(id="OADMIC0022",name="ADMIncidentTimeline")
    @Operation(operationId="admIncidentFindTimeline",summary="Incident immutable Timeline 조회")
    public ResponseEntity<List<TimelineResponse>> findTimeline(@PathVariable long incidentId,HttpServletRequest request){
        operator(request);return ResponseEntity.ok(service.findTimeline(incidentId));}

    @PostMapping("/{incidentId}/acknowledge")
    @CpfOnlineTransaction(id="OADMIC0030",name="ADMIncidentAcknowledge")
    @Operation(operationId="admIncidentAcknowledge",summary="Incident 접수")
    public ResponseEntity<IncidentResponse> acknowledge(@PathVariable long incidentId,@RequestBody IncidentActionRequest body,HttpServletRequest request){
        return action(incidentId,"ACKNOWLEDGE",body,request);}

    @PostMapping("/{incidentId}/resolve")
    @CpfOnlineTransaction(id="OADMIC0031",name="ADMIncidentResolve")
    @Operation(operationId="admIncidentResolve",summary="Incident 해결")
    public ResponseEntity<IncidentResponse> resolve(@PathVariable long incidentId,@RequestBody IncidentActionRequest body,HttpServletRequest request){
        return action(incidentId,"RESOLVE",body,request);}

    @PostMapping("/{incidentId}/reopen")
    @CpfOnlineTransaction(id="OADMIC0032",name="ADMIncidentReopen")
    @Operation(operationId="admIncidentReopen",summary="Incident 재개방")
    public ResponseEntity<IncidentResponse> reopen(@PathVariable long incidentId,@RequestBody IncidentActionRequest body,HttpServletRequest request){
        return action(incidentId,"REOPEN",body,request);}

    @PostMapping("/{incidentId}/escalate")
    @CpfOnlineTransaction(id="OADMIC0033",name="ADMIncidentEscalate")
    @Operation(operationId="admIncidentEscalate",summary="Incident 수동 Escalation")
    public ResponseEntity<IncidentResponse> escalate(@PathVariable long incidentId,@RequestBody IncidentActionRequest body,HttpServletRequest request){
        return action(incidentId,"ESCALATE",body,request);}

    @GetMapping("/maintenance-windows")
    @CpfOnlineTransaction(id="OADMIC0040",name="ADMMaintenancePage")
    @Operation(operationId="admIncidentFindMaintenance",summary="Maintenance Window Page 조회")
    public ResponseEntity<Page<MaintenanceResponse>> findMaintenance(
            @RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="50") int size,HttpServletRequest request){
        operator(request);return ResponseEntity.ok(service.findMaintenance(page,size));}

    @PostMapping("/maintenance-windows")
    @CpfOnlineTransaction(id="OADMIC0041",name="ADMMaintenanceCreate")
    @Operation(operationId="admIncidentCreateMaintenance",summary="Maintenance Window 등록")
    public ResponseEntity<MaintenanceResponse> createMaintenance(@RequestBody MaintenanceSaveRequest body,HttpServletRequest request){
        return ResponseEntity.ok(service.saveMaintenance(null,body,operator(request),request.getRemoteAddr()));}

    @PutMapping("/maintenance-windows/{maintenanceId}")
    @CpfOnlineTransaction(id="OADMIC0042",name="ADMMaintenanceUpdate")
    @Operation(operationId="admIncidentUpdateMaintenance",summary="Maintenance Window 수정·취소")
    public ResponseEntity<MaintenanceResponse> updateMaintenance(@PathVariable long maintenanceId,@RequestBody MaintenanceSaveRequest body,HttpServletRequest request){
        return ResponseEntity.ok(service.saveMaintenance(maintenanceId,body,operator(request),request.getRemoteAddr()));}

    @ExceptionHandler(AdmIncidentConflictException.class)
    public ResponseEntity<Map<String, Object>> incidentConflict(AdmIncidentConflictException exception) {
        HttpStatus status = exception.type() == AdmIncidentConflictException.Type.NOT_FOUND
                ? HttpStatus.NOT_FOUND : HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(Map.of(
                "code", "CPF-ADM-INCIDENT-" + exception.type().name(),
                "message", exception.getMessage(),
                "retryable", exception.type() == AdmIncidentConflictException.Type.COMMAND_IN_PROGRESS));
    }

    private ResponseEntity<IncidentResponse> action(long incidentId,String action,IncidentActionRequest body,HttpServletRequest request){
        return ResponseEntity.ok(service.transition(incidentId,action,body,operator(request),request.getRemoteAddr()));}
    private String operator(HttpServletRequest request){Object value=request.getAttribute("adm.operatorId");
        if(!(value instanceof String id)||id.isBlank())throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"verified ADM operator required");return id;}
}
