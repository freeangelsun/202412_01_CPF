package com.cpf.batch.control.deploy;

import com.cpf.batch.api.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class CellOperationsService {
    private final JdbcTemplate jdbc; private final RuntimeLifecycleService lifecycle;
    public CellOperationsService(JdbcTemplate jdbc,RuntimeLifecycleService lifecycle){this.jdbc=jdbc;this.lifecycle=lifecycle;}

    public Map<String,Object> status(String cellId) {
        Map<String,Object> cell=jdbc.queryForMap("SELECT * FROM bat_deployment_cell WHERE cell_id=?",cellId);
        List<Map<String,Object>> instances=jdbc.queryForList("""
          SELECT d.instance_id,d.host_alias,d.zone_id,d.pool_id,d.desired_state,r.actual_state,r.last_heartbeat_at,r.artifact_version,r.row_version,
            COALESCE((SELECT h.current_execution_count FROM bat_runtime_heartbeat h WHERE h.instance_id=d.instance_id ORDER BY h.heartbeat_at DESC LIMIT 1),0) current_execution_count
          FROM bat_deployment_instance d LEFT JOIN bat_runtime_instance r ON r.instance_id=d.instance_id
          WHERE d.cell_id=? ORDER BY d.instance_id
          """,cellId);
        return Map.of("cell",cell,"instances",instances);
    }

    public OperationResult scale(String cellId,int desired,ApprovedRequest approval) {
        approve(approval);List<Map<String,Object>> inventory=jdbc.queryForList("""
          SELECT d.instance_id,COALESCE(r.actual_state,'STOPPED') actual_state,
            COALESCE((SELECT h.current_execution_count FROM bat_runtime_heartbeat h WHERE h.instance_id=d.instance_id ORDER BY h.heartbeat_at DESC LIMIT 1),0) current_execution_count
          FROM bat_deployment_instance d LEFT JOIN bat_runtime_instance r ON r.instance_id=d.instance_id
          WHERE d.cell_id=? ORDER BY d.instance_id
          """,cellId);
        if(desired<0||desired>inventory.size())throw new IllegalArgumentException("desired count must be within approved cell inventory");
        int running=(int)inventory.stream().filter(this::running).count();List<Map<String,Object>> results=new ArrayList<>();
        if(desired>running){
            int need=desired-running;
            for(var row:inventory){if(need==0)break;if(running(row))continue;String id=Objects.toString(row.get("instance_id"));var r=lifecycle.operate(id,"start",approval.requestedBy(),approval.reason());results.add(Map.of("instanceId",id,"operation","START","state",r.state().name()));if(r.state()==CommandState.SUCCEEDED)need--;}
            if(need>0)return new OperationResult("PARTIAL",desired,results,"Not enough instances could be started");
        } else if(desired<running){
            int remove=running-desired;List<Map<String,Object>> reversed=new ArrayList<>(inventory);Collections.reverse(reversed);
            for(var row:reversed){if(remove==0)break;if(!running(row))continue;String id=Objects.toString(row.get("instance_id"));
                var drain=lifecycle.operate(id,"drain",approval.requestedBy(),approval.reason());results.add(Map.of("instanceId",id,"operation","DRAIN","state",drain.state().name()));if(drain.state()!=CommandState.SUCCEEDED)continue;
                if(!waitIdle(id,Duration.ofMinutes(10))){results.add(Map.of("instanceId",id,"operation","STOP","state","UNKNOWN_RESULT"));continue;}
                var stop=lifecycle.operate(id,"stop",approval.requestedBy(),approval.reason());results.add(Map.of("instanceId",id,"operation","STOP","state",stop.state().name()));if(stop.state()==CommandState.SUCCEEDED)remove--;
            }
            if(remove>0)return new OperationResult("PARTIAL",desired,results,"Scale-in incomplete; busy/unknown instances preserved");
        }
        jdbc.update("UPDATE bat_deployment_cell SET desired_instance_count=?,updated_at=CURRENT_TIMESTAMP(6) WHERE cell_id=?",desired,cellId);
        audit(cellId,"SCALE",approval,"desired="+desired+",results="+results.size());
        return new OperationResult("SUCCEEDED",desired,results,"Scale completed");
    }

    public OperationResult reconcile(String cellId,ApprovedRequest approval) {
        approve(approval);List<Map<String,Object>> rows=jdbc.queryForList("""
          SELECT d.instance_id,d.desired_state,COALESCE(r.actual_state,'STOPPED') actual_state,r.last_heartbeat_at
          FROM bat_deployment_instance d LEFT JOIN bat_runtime_instance r ON r.instance_id=d.instance_id WHERE d.cell_id=? ORDER BY d.instance_id
          """,cellId);List<Map<String,Object>> out=new ArrayList<>();
        for(var row:rows){String id=Objects.toString(row.get("instance_id")),desired=Objects.toString(row.get("desired_state"),"RUNNING"),actual=Objects.toString(row.get("actual_state"),"STOPPED");String op=null;
            if("RUNNING".equals(desired)&&!Set.of("READY","BUSY","STARTING").contains(actual))op="start";
            else if("STOPPED".equals(desired)&&!"STOPPED".equals(actual))op="stop";
            else if("DRAINING".equals(desired)&&!"DRAINING".equals(actual))op="drain";
            if(op!=null){var r=lifecycle.operate(id,op,approval.requestedBy(),approval.reason());out.add(Map.of("instanceId",id,"operation",op.toUpperCase(Locale.ROOT),"state",r.state().name()));}
        }
        audit(cellId,"RECONCILE",approval,"actions="+out.size());return new OperationResult("SUCCEEDED",null,out,"Reconciliation dispatched");
    }

    private boolean running(Map<String,Object> row){return Set.of("STARTING","READY","BUSY","DRAINING","DEGRADED").contains(Objects.toString(row.get("actual_state"),""));}
    private boolean waitIdle(String instanceId,Duration timeout){Instant end=Instant.now().plus(timeout);while(Instant.now().isBefore(end)){Integer count=jdbc.queryForObject("SELECT COALESCE((SELECT current_execution_count FROM bat_runtime_heartbeat WHERE instance_id=? ORDER BY heartbeat_at DESC LIMIT 1),0)",Integer.class,instanceId);if(count!=null&&count==0)return true;try{Thread.sleep(500);}catch(InterruptedException e){Thread.currentThread().interrupt();return false;}}return false;}
    private void approve(ApprovedRequest a){if(a==null||a.requestedBy()==null||a.requestedBy().isBlank()||a.approvedBy()==null||a.approvedBy().isBlank()||a.requestedBy().equals(a.approvedBy())||a.reason()==null||a.reason().isBlank())throw new IllegalArgumentException("requester/approver separation and reason required");}
    private void audit(String cell,String op,ApprovedRequest a,String result){jdbc.update("""
INSERT INTO bat_operation_log(job_id,operation_type,operator_id,reason,after_data,result_type,result_message,created_by,updated_by)
      VALUES('SYSTEM',?,?,?,?, 'S','OK',?,?)""",op,a.requestedBy(),a.reason(),"cell="+cell+","+result,a.requestedBy(),a.requestedBy());}
    public record ApprovedRequest(String requestedBy,String approvedBy,String reason){}
    public record OperationResult(String state,Integer desiredCount,List<Map<String,Object>> instances,String message){}
}
