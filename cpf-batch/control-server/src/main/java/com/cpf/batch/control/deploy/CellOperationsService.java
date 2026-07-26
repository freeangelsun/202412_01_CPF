package com.cpf.batch.control.deploy;

import com.cpf.batch.api.*;
import com.cpf.core.common.database.CpfVendorSqlCatalog;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;

@Service
public class CellOperationsService {
    private final JdbcTemplate jdbc;
    private final RuntimeLifecycleService lifecycle;
    private final CpfVendorSqlCatalog sql;

    public CellOperationsService(
            JdbcTemplate jdbc,
            RuntimeLifecycleService lifecycle,
            Environment environment) {
        this.jdbc = jdbc;
        this.lifecycle = lifecycle;
        this.sql = CpfVendorSqlCatalog.create(environment, "bat");
    }

    public Map<String,Object> status(String cellId) {
        Map<String,Object> cell =
                jdbc.queryForMap(sql.required("deploy-cell-detail"), cellId);
        List<Map<String,Object>> instances =
                jdbc.queryForList(sql.required("deploy-cell-status-instances"), cellId);
        return Map.of("cell",cell,"instances",instances);
    }

    public OperationResult scale(String cellId,int desired,ApprovedRequest approval) {
        approve(approval);
        List<Map<String,Object>> inventory =
                jdbc.queryForList(sql.required("deploy-cell-scale-inventory"), cellId);
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
        jdbc.update(sql.required("deploy-cell-update-desired-count"), desired, cellId);
        audit(cellId,"SCALE",approval,"desired="+desired+",results="+results.size());
        return new OperationResult("SUCCEEDED",desired,results,"Scale completed");
    }

    public OperationResult reconcile(String cellId,ApprovedRequest approval) {
        approve(approval);
        List<Map<String,Object>> rows =
                jdbc.queryForList(sql.required("deploy-cell-reconcile-inventory"), cellId);
        List<Map<String,Object>> out=new ArrayList<>();
        for(var row:rows){String id=Objects.toString(row.get("instance_id")),desired=Objects.toString(row.get("desired_state"),"RUNNING"),actual=Objects.toString(row.get("actual_state"),"STOPPED");String op=null;
            if("RUNNING".equals(desired)&&!Set.of("READY","BUSY","STARTING").contains(actual))op="start";
            else if("STOPPED".equals(desired)&&!"STOPPED".equals(actual))op="stop";
            else if("DRAINING".equals(desired)&&!"DRAINING".equals(actual))op="drain";
            if(op!=null){var r=lifecycle.operate(id,op,approval.requestedBy(),approval.reason());out.add(Map.of("instanceId",id,"operation",op.toUpperCase(Locale.ROOT),"state",r.state().name()));}
        }
        audit(cellId,"RECONCILE",approval,"actions="+out.size());return new OperationResult("SUCCEEDED",null,out,"Reconciliation dispatched");
    }

    private boolean running(Map<String,Object> row){return Set.of("STARTING","READY","BUSY","DRAINING","DEGRADED").contains(Objects.toString(row.get("actual_state"),""));}
    private boolean waitIdle(String instanceId,Duration timeout){Instant end=Instant.now().plus(timeout);while(Instant.now().isBefore(end)){Integer count=jdbc.queryForObject(sql.required("deploy-runtime-current-execution-count"),Integer.class,instanceId);if(count!=null&&count==0)return true;try{Thread.sleep(500);}catch(InterruptedException e){Thread.currentThread().interrupt();return false;}}return false;}
    private void approve(ApprovedRequest a){if(a==null||a.requestedBy()==null||a.requestedBy().isBlank()||a.approvedBy()==null||a.approvedBy().isBlank()||a.requestedBy().equals(a.approvedBy())||a.reason()==null||a.reason().isBlank())throw new IllegalArgumentException("requester/approver separation and reason required");}
    private void audit(String cell,String op,ApprovedRequest a,String result){jdbc.update(
            sql.required("deploy-cell-operation-audit"),
            op,a.requestedBy(),a.reason(),"cell="+cell+","+result,a.requestedBy(),a.requestedBy());}
    public record ApprovedRequest(String requestedBy,String approvedBy,String reason){}
    public record OperationResult(String state,Integer desiredCount,List<Map<String,Object>> instances,String message){}
}
