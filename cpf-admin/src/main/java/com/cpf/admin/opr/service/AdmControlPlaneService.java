package com.cpf.admin.opr.service;

import com.cpf.core.api.servicecall.CpfServiceRegistryControlPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * ADM Incident/Maintenance control plane runtime입니다.
 * Incident lifecycle은 admDB가 소유하고, 실제 서비스 상태 변경은 Service Registry owner port로 위임합니다.
 */
@Service
public class AdmControlPlaneService extends com.cpf.admin.common.base.AdmBaseService {
    private final JdbcTemplate admJdbcTemplate;
    private final CpfServiceRegistryControlPort serviceControlPort;

    public AdmControlPlaneService(
            @Qualifier("admJdbcTemplate") JdbcTemplate admJdbcTemplate,
            CpfServiceRegistryControlPort serviceControlPort) {
        this.admJdbcTemplate = admJdbcTemplate;
        this.serviceControlPort = serviceControlPort;
    }

    public List<Map<String, Object>> findIncidents(String status, String severity, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT incident_id AS incidentId, incident_no AS incidentNo, severity, title, summary,
                       source_type AS sourceType, source_id AS sourceId, status, detected_at AS detectedAt,
                       acknowledged_at AS acknowledgedAt, mitigated_at AS mitigatedAt,
                       resolved_at AS resolvedAt, created_by AS createdBy, updated_by AS updatedBy,
                       reason, version
                FROM adm_incident
                WHERE 1 = 1
                """);
        java.util.ArrayList<Object> args = new java.util.ArrayList<>();
        if (hasText(status)) {
            sql.append(" AND status = ?");
            args.add(status.toUpperCase(Locale.ROOT));
        }
        if (hasText(severity)) {
            sql.append(" AND severity = ?");
            args.add(severity.toUpperCase(Locale.ROOT));
        }
        sql.append("""
                 ORDER BY CASE severity
                            WHEN 'SEV1' THEN 1
                            WHEN 'SEV2' THEN 2
                            WHEN 'SEV3' THEN 3
                            ELSE 4
                          END,
                          detected_at DESC
                """);
        return AdmJdbcQueries.queryForList(
                admJdbcTemplate,
                sql.toString(),
                args,
                Math.max(1, Math.min(limit, 500)));
    }

    public Map<String,Object> createIncident(Map<String,Object> request,String operatorId){
        String severity=text(request.get("severity"),"SEV2").toUpperCase(Locale.ROOT);
        String title=required(request.get("title"),"title");
        String summary=text(request.get("summary"),"");
        String sourceType=text(request.get("sourceType"),"MANUAL");
        String sourceId=text(request.get("sourceId"),"");
        String reason=required(request.get("reason"),"reason");
        String no="INC-"+java.time.LocalDate.now().toString().replace("-","")+"-"+Long.toString(System.currentTimeMillis(),36).toUpperCase(Locale.ROOT);
        admJdbcTemplate.update("""
            INSERT INTO adm_incident(incident_no,severity,title,summary,source_type,source_id,status,detected_at,created_by,updated_by,reason,version)
            VALUES(?,?,?,?,?,?,'OPEN',CURRENT_TIMESTAMP,?,?,?,0)
            """,no,severity,title,summary,sourceType,sourceId,operatorId,operatorId,reason);
        return admJdbcTemplate.queryForMap("SELECT * FROM adm_incident WHERE incident_no=?",no);
    }

    public Map<String,Object> transitionIncident(long incidentId,String targetStatus,String reason,String operatorId){
        Map<String,Object> current=admJdbcTemplate.queryForMap("SELECT incident_id,status,version FROM adm_incident WHERE incident_id=?",incidentId);
        String from=String.valueOf(current.get("status")); String to=required(targetStatus,"status").toUpperCase(Locale.ROOT);
        if(!allowedTransition(from,to)) throw new IllegalArgumentException("Invalid incident transition: "+from+" -> "+to);
        String timeColumn=switch(to){case "ACKNOWLEDGED"->"acknowledged_at";case "MITIGATED"->"mitigated_at";case "RESOLVED","CLOSED"->"resolved_at";default->null;};
        long version=((Number)current.get("version")).longValue();
        String sql="UPDATE adm_incident SET status=?, updated_by=?, reason=?, version=version+1, updated_at=CURRENT_TIMESTAMP"+(timeColumn==null?"":", "+timeColumn+"=CURRENT_TIMESTAMP")+" WHERE incident_id=? AND version=?";
        int n=admJdbcTemplate.update(sql,to,operatorId,required(reason,"reason"),incidentId,version);
        if(n!=1) throw new IllegalStateException("Incident was concurrently modified. incidentId="+incidentId);
        return admJdbcTemplate.queryForMap("SELECT * FROM adm_incident WHERE incident_id=?",incidentId);
    }

    public List<Map<String, Object>> findMaintenanceActions(int limit) {
        return AdmJdbcQueries.queryForList(
                admJdbcTemplate,
                """
                SELECT action_id AS actionId, service_id AS serviceId, endpoint_code AS endpointCode,
                       instance_id AS instanceId, action_type AS actionType, before_status AS beforeStatus,
                       after_status AS afterStatus, result_status AS resultStatus, reason,
                       requested_by AS requestedBy, requested_at AS requestedAt, result_detail AS resultDetail
                FROM adm_maintenance_action
                ORDER BY action_id DESC
                """,
                List.of(),
                Math.max(1, Math.min(limit, 500)));
    }

    public Map<String,Object> executeMaintenance(Map<String,Object> request,String operatorId){
        String serviceId=required(request.get("serviceId"),"serviceId"); String endpointCode=required(request.get("endpointCode"),"endpointCode"); String instanceId=required(request.get("instanceId"),"instanceId");
        String action=required(request.get("action"),"action").toUpperCase(Locale.ROOT); String reason=required(request.get("reason"),"reason");
        CpfServiceRegistryControlPort.InstanceCommand command=CpfServiceRegistryControlPort.InstanceCommand.valueOf(action);
        Map<String,Object> result; String resultStatus="SUCCESS"; String detail;
        try{result=serviceControlPort.changeInstanceState(serviceId,endpointCode,instanceId,command,reason,operatorId);detail=String.valueOf(result);}
        catch(RuntimeException ex){resultStatus="FAILED";detail=ex.getMessage();recordMaintenance(serviceId,endpointCode,instanceId,action,null,null,resultStatus,reason,operatorId,detail);throw ex;}
        recordMaintenance(serviceId,endpointCode,instanceId,action,null,String.valueOf(result.get("instanceStatus")),resultStatus,reason,operatorId,detail);
        return result;
    }

    private void recordMaintenance(String serviceId,String endpointCode,String instanceId,String action,String before,String after,String status,String reason,String user,String detail){
        admJdbcTemplate.update("INSERT INTO adm_maintenance_action(service_id,endpoint_code,instance_id,action_type,before_status,after_status,result_status,reason,requested_by,requested_at,result_detail) VALUES(?,?,?,?,?,?,?,?,?,CURRENT_TIMESTAMP,?)",serviceId,endpointCode,instanceId,action,before,after,status,reason,user,detail);
    }
    private boolean allowedTransition(String from,String to){
        return switch(from){case "OPEN"->to.equals("ACKNOWLEDGED")||to.equals("MITIGATED")||to.equals("RESOLVED");case "ACKNOWLEDGED"->to.equals("MITIGATED")||to.equals("RESOLVED");case "MITIGATED"->to.equals("RESOLVED");case "RESOLVED"->to.equals("CLOSED")||to.equals("OPEN");case "CLOSED"->to.equals("OPEN");default->false;};
    }
    private static boolean hasText(String v){return v!=null&&!v.isBlank();}
    private static String required(Object v,String name){String s=v==null?"":String.valueOf(v).trim();if(s.isEmpty())throw new IllegalArgumentException(name+" is required");return s;}
    private static String text(Object v,String d){String s=v==null?"":String.valueOf(v).trim();return s.isEmpty()?d:s;}
}
