package com.cpf.admin.opr.service;

import com.cpf.admin.common.base.AdmBaseService;
import com.cpf.admin.opr.repository.AdmControlPlaneRepository;
import com.cpf.foundation.annotation.CpfService;
import com.cpf.integration.api.servicecall.CpfServiceRegistryControlPort;
import com.cpf.integration.api.servicecall.CpfServiceRegistryView;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ADM Incident/Maintenance Control-Plane의 업무 Service입니다.
 * 상태는 CPF_PLATFORM_DB(cpfDB)에 기록하고 실제 Runtime 변경은 Service Registry Owner Port로 위임합니다.
 */
// CPF stereotype 이 붙은 Business Type 은 proxy-safe 여야 한다.
// CpfCapabilityUsageAspect.proxySafeBusinessType() 이 final Type 을 proxy-unsafe 로 판정하고,
// Advisor 가 매칭되면 CGLIB subclass 생성이 불가능해 Runtime 기동이 실패한다.
@CpfService
public class AdmControlPlaneService extends AdmBaseService {
    private final AdmControlPlaneRepository repository;
    private final CpfServiceRegistryControlPort serviceControlPort;

    public AdmControlPlaneService(
            AdmControlPlaneRepository repository, CpfServiceRegistryControlPort serviceControlPort) {
        this.repository = repository;
        this.serviceControlPort = serviceControlPort;
    }

    /** findIncidents 작업을 CPF 표준 계약에 따라 수행한다. */
    public List<Map<String, Object>> findIncidents(String status, String severity, int limit) {
        return repository.findIncidents(status, severity, limit);
    }

    public Map<String,Object> createIncident(Map<String,Object> request,String operatorId){
        String severity=text(request.get("severity"),"SEV2").toUpperCase(Locale.ROOT);
        String title=required(request.get("title"),"title");
        String summary=text(request.get("summary"),"");
        String sourceType=text(request.get("sourceType"),"MANUAL");
        String sourceId=text(request.get("sourceId"),"");
        String reason=auditReason(String.valueOf(request.get("reason")));
        String no="INC-"+java.time.LocalDate.now().toString().replace("-","")+"-"+Long.toString(System.currentTimeMillis(),36).toUpperCase(Locale.ROOT);
        return repository.createIncident(no,severity,title,summary,sourceType,sourceId,required(operatorId,"operatorId"),reason);
    }

    /** transitionIncident 작업을 CPF 표준 계약에 따라 수행한다. */
    public Map<String,Object> transitionIncident(long incidentId,String targetStatus,String reason,String operatorId){
        Map<String,Object> current=repository.incident(incidentId);
        String from=String.valueOf(current.get("status"));
        String to=required(targetStatus,"status").toUpperCase(Locale.ROOT);
        if(!allowedTransition(from,to)) throw new IllegalArgumentException("Invalid incident transition: "+from+" -> "+to);
        String timeColumn=switch(to){case "ACKNOWLEDGED"->"acknowledged_at";case "MITIGATED"->"mitigated_at";case "RESOLVED","CLOSED"->"resolved_at";default->null;};
        long version=((Number)current.get("version")).longValue();
        if(!repository.transitionIncident(incidentId,version,to,required(operatorId,"operatorId"),auditReason(reason),timeColumn)) {
            throw new IllegalStateException("Incident was concurrently modified. incidentId="+incidentId);
        }
        return repository.incidentDetail(incidentId);
    }

    /** findMaintenanceActions 작업을 CPF 표준 계약에 따라 수행한다. */
    public List<Map<String, Object>> findMaintenanceActions(int limit) {
        return repository.findMaintenanceActions(limit);
    }

    /** 승인 Owner Adapter에서 검증이 끝난 Instance 상태 전환만 실행합니다. */
    public CpfServiceRegistryView.MutationResult executeApprovedMaintenance(
            String operationId, String serviceId, String endpointCode, String instanceId,
            CpfServiceRegistryControlPort.InstanceCommand action, long expectedVersion,
            String reason, String approvedBy) {
        var command = new CpfServiceRegistryControlPort.InstanceStateCommand(
                operationCode(operationId), action, expectedVersion, auditReason(reason), required(approvedBy,"approvedBy"));
        try {
            CpfServiceRegistryView.MutationResult result =
                    serviceControlPort.changeInstanceState(serviceId, endpointCode, instanceId, command);
            repository.recordMaintenance(serviceId,endpointCode,instanceId,action.name(),null,result.status(),
                    "SUCCESS",reason,approvedBy,result.toString());
            return result;
        // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
        } catch (RuntimeException ex) {
            repository.recordMaintenance(serviceId,endpointCode,instanceId,action.name(),null,null,
                    "FAILED",reason,approvedBy,ex.getClass().getSimpleName());
            throw ex;
        }
    }

    /** 승인 Owner Adapter가 검증한 Service Registry 삭제만 실제 Owner Port에 전달합니다. */
    public CpfServiceRegistryView.MutationResult executeApprovedRegistryDelete(
            String operationId, String targetType, String targetId, long expectedVersion,
            String reason, String approvedBy) {
        var command = new CpfServiceRegistryControlPort.DeleteCommand(
                operationCode(operationId), expectedVersion, auditReason(reason), required(approvedBy, "approvedBy"));
        return switch (required(targetType, "targetType").toUpperCase(Locale.ROOT)) {
            case "SERVICE_REGISTRY_SERVICE" -> serviceControlPort.deleteService(required(targetId, "targetId"), command);
            case "SERVICE_REGISTRY_ENDPOINT" -> serviceControlPort.deleteEndpoint(required(targetId, "targetId"), command);
            case "SERVICE_REGISTRY_INSTANCE" -> serviceControlPort.deleteInstance(required(targetId, "targetId"), command);
            default -> throw new IllegalArgumentException("지원하지 않는 Service Registry 삭제 대상입니다: " + targetType);
        };
    }

    private boolean allowedTransition(String from,String to){
        return switch(from){case "OPEN"->to.equals("ACKNOWLEDGED")||to.equals("MITIGATED")||to.equals("RESOLVED");case "ACKNOWLEDGED"->to.equals("MITIGATED")||to.equals("RESOLVED");case "MITIGATED"->to.equals("RESOLVED");case "RESOLVED"->to.equals("CLOSED")||to.equals("OPEN");case "CLOSED"->to.equals("OPEN");default->false;};
    }
    private static String required(Object v,String name){String s=v==null?"":String.valueOf(v).trim();if(s.isEmpty())throw new IllegalArgumentException(name+" is required");return s;}
    private static String text(Object v,String d){String s=v==null?"":String.valueOf(v).trim();return s.isEmpty()?d:s;}
}
