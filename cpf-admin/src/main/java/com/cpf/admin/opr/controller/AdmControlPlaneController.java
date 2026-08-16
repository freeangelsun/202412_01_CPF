package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.admin.opr.service.AdmControlPlaneService;
import com.cpf.foundation.annotation.CpfOnlineTransaction;
import com.cpf.core.api.context.CpfContexts;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.cpf.web.api.CpfController;
import java.util.List;import java.util.Map;

/** Incident·Runtime Control Plane의 상태 조회와 안전한 운영조치를 제공합니다. */
@CpfController
@Tag(name="ADM-ControlPlane",description="Incident, maintenance and service drain control")
public class AdmControlPlaneController extends com.cpf.admin.common.base.AdmBaseController {
    private final AdmControlPlaneService service; private final AdmAuditLogService audit;
    public AdmControlPlaneController(AdmControlPlaneService service,AdmAuditLogService audit){this.service=service;this.audit=audit;}

    @PostMapping("/adm/api/incidents") @CpfOnlineTransaction(id="OADMIC0002",name="ADMIncidentCreate", ownerDomain="ADM") @Operation(operationId="admIncidentCreateIncident", summary="Incident 생성")
    /** createIncident 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<Map<String,Object>> createIncident(@RequestBody Map<String,Object> body,HttpServletRequest request){String user=operator(request);Map<String,Object>
            result=service.createIncident(body,user);audit(request,user,"INCIDENT_CREATE","adm_incident",String.valueOf(result.get("INCIDENT_ID")),String.valueOf(body.get("reason")),result);
            return ResponseEntity.ok(result);}
    @PostMapping("/adm/api/incidents/{incidentId}/status") @CpfOnlineTransaction(id="OADMIC0003",name="ADMIncidentTransition", ownerDomain="ADM") @Operation(operationId="admIncidentTransitionIncident", summary="Incident 상태 전이")
    public ResponseEntity<Map<String,Object>> transition(@PathVariable long incidentId,@RequestBody Map<String,Object> body,HttpServletRequest request){String user=operator(request);String
            reason=String.valueOf(body.getOrDefault("reason",""));Map<String,Object> result=service.transitionIncident(incidentId,String.valueOf(body.get("status")),reason,user);
            audit(request,user,"INCIDENT_TRANSITION","adm_incident",String.valueOf(incidentId),reason,result);return ResponseEntity.ok(result);}
    @GetMapping("/adm/api/maintenance/actions") @CpfOnlineTransaction(id="OADMMT0001",name="ADMMaintenanceList", ownerDomain="ADM") @Operation(operationId="admMaintenanceFindActions", summary="Maintenance 명령 이력")
    /** maintenance 작업을 CPF 표준 계약에 따라 수행한다. */
    public ResponseEntity<List<Map<String,Object>>> maintenance(@RequestParam(defaultValue="100")int limit){return ResponseEntity.ok(service.findMaintenanceActions(limit));}
    /**
     * 과거 direct mutation endpoint는 승인 Engine 우회를 방지하기 위해 fail-closed 합니다.
     * 실제 Drain/Disable/Resume은 /adm/api/approvals 요청/승인/execute 경로에서
     * ServiceRegistryApprovalOwnerCommandAdapter를 통해 Owner Port로 실행합니다.
     */
    @PostMapping("/adm/api/maintenance/actions")
    @CpfOnlineTransaction(id="OADMMT0002",name="ADMMaintenanceExecute", ownerDomain="ADM")
    @Operation(operationId="admMaintenanceExecuteAction", summary="Service instance 변경 승인 경로 안내")
    public ResponseEntity<Map<String,Object>> executeMaintenance(@RequestBody Map<String,Object> body,HttpServletRequest request){
        String user=operator(request);
        String reason=String.valueOf(body.getOrDefault("reason",""));
        audit(request,user,"SERVICE_INSTANCE_CONTROL_APPROVAL_REQUIRED","cpf_service_instance",
                String.valueOf(body.get("instanceId")),reason,Map.of("status","APPROVAL_REQUIRED"));
        return ResponseEntity.status(409).body(Map.of(
                "status","APPROVAL_REQUIRED",
                "message","Service instance 변경은 ADM 승인 요청/승인/실행 경로를 사용해야 합니다.",
                "approvalEndpoint","/adm/api/approvals/requests"));
    }

    private String operator(HttpServletRequest req){Object value=req.getAttribute("adm.operatorId");if(value instanceof String s&&!s.isBlank())return s;throw new IllegalStateException("ADM operator context가 필요합니다.");}
    private void audit(HttpServletRequest req,String user,String action,String type,String id,String reason,Object after){audit.record(CpfContexts.transactionId(),user,action,type,
            id,reason,"",String.valueOf(after),"상태 변경",req.getRemoteAddr());}
}
