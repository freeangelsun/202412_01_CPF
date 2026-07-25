package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmControlPlaneService;
import com.cpf.core.common.execution.CpfOnlineTransaction;
import com.cpf.core.common.logging.TransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;import java.util.Map;

@RestController
@Tag(name="ADM-ControlPlane",description="Incident, maintenance and service drain control")
public class AdmControlPlaneController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmControlPlaneService service; private final AdmAuditLogService audit;
    public AdmControlPlaneController(AdmControlPlaneService service,AdmAuditLogService audit){this.service=service;this.audit=audit;}

    @GetMapping("/adm/api/incidents") @CpfOnlineTransaction(id="OADMIC0001",name="ADMIncidentList") @Operation(summary="Incident 목록")
    public ResponseEntity<List<Map<String,Object>>> incidents(@RequestParam(required=false)String status,@RequestParam(required=false)String severity,@RequestParam(defaultValue="100")int limit){return ResponseEntity.ok(service.findIncidents(status,severity,limit));}
    @PostMapping("/adm/api/incidents") @CpfOnlineTransaction(id="OADMIC0002",name="ADMIncidentCreate") @Operation(summary="Incident 생성")
    public ResponseEntity<Map<String,Object>> createIncident(@RequestBody Map<String,Object> body,HttpServletRequest request){String user=operator(request);Map<String,Object> result=service.createIncident(body,user);audit(request,user,"INCIDENT_CREATE","adm_incident",String.valueOf(result.get("INCIDENT_ID")),String.valueOf(body.get("reason")),result);return ResponseEntity.ok(result);}
    @PostMapping("/adm/api/incidents/{incidentId}/status") @CpfOnlineTransaction(id="OADMIC0003",name="ADMIncidentTransition") @Operation(summary="Incident 상태 전이")
    public ResponseEntity<Map<String,Object>> transition(@PathVariable long incidentId,@RequestBody Map<String,Object> body,HttpServletRequest request){String user=operator(request);String reason=String.valueOf(body.getOrDefault("reason",""));Map<String,Object> result=service.transitionIncident(incidentId,String.valueOf(body.get("status")),reason,user);audit(request,user,"INCIDENT_TRANSITION","adm_incident",String.valueOf(incidentId),reason,result);return ResponseEntity.ok(result);}
    @GetMapping("/adm/api/maintenance/actions") @CpfOnlineTransaction(id="OADMMT0001",name="ADMMaintenanceList") @Operation(summary="Maintenance 명령 이력")
    public ResponseEntity<List<Map<String,Object>>> maintenance(@RequestParam(defaultValue="100")int limit){return ResponseEntity.ok(service.findMaintenanceActions(limit));}
    @PostMapping("/adm/api/maintenance/actions") @CpfOnlineTransaction(id="OADMMT0002",name="ADMMaintenanceExecute") @Operation(summary="Service instance Drain/Disable/Resume")
    public ResponseEntity<Map<String,Object>> executeMaintenance(@RequestBody Map<String,Object> body,HttpServletRequest request){String user=operator(request);String reason=String.valueOf(body.getOrDefault("reason",""));Map<String,Object> result=service.executeMaintenance(body,user);audit(request,user,"SERVICE_INSTANCE_CONTROL","cpf_service_instance",String.valueOf(body.get("instanceId")),reason,result);return ResponseEntity.ok(result);}

    private String operator(HttpServletRequest req){Object value=req.getAttribute("adm.operatorId");return value instanceof String s&&!s.isBlank()?s:"admin-ui";}
    private void audit(HttpServletRequest req,String user,String action,String type,String id,String reason,Object after){audit.record(TransactionContext.getOrCreateTransactionId(),user,action,type,id,reason,"",String.valueOf(after),"상태 변경",req.getRemoteAddr());}
}
