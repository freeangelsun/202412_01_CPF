package com.cpf.batch.control.deploy;

import com.cpf.batch.api.*;
import com.cpf.batch.runtime.SensitiveTextSanitizer;
import com.cpf.batch.spi.*;
import com.cpf.core.api.database.CpfVendorSqlCatalog;
import com.cpf.core.api.database.CpfVendorSqlCatalogProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.*;

@Service
public class DeploymentEngine {
    private final List<DeploymentTargetAdapter> adapters; private final RuntimeHealthProbe health;
    private final CompatibilityService compatibility; private final JdbcTemplate jdbc; private final DeploymentExecutionRepository executions;
    private final DeploymentCellLock cellLock;
    private final CpfVendorSqlCatalog sql;
    @Autowired
    public DeploymentEngine(List<DeploymentTargetAdapter> adapters,RuntimeHealthProbe health,CompatibilityService compatibility,
                            JdbcTemplate jdbc,DeploymentExecutionRepository executions,DeploymentCellLock cellLock,
                            CpfVendorSqlCatalogProvider sqlCatalogProvider){
        this(adapters,health,compatibility,jdbc,executions,cellLock,
                sqlCatalogProvider.forModule("bat"));
    }
    DeploymentEngine(List<DeploymentTargetAdapter> adapters,RuntimeHealthProbe health,CompatibilityService compatibility,
                     JdbcTemplate jdbc,DeploymentExecutionRepository executions,DeploymentCellLock cellLock,
                     CpfVendorSqlCatalog sql){
        this.adapters=adapters;this.health=health;this.compatibility=compatibility;this.jdbc=jdbc;this.executions=executions;
        this.cellLock=cellLock;
        this.sql=sql;
    }

    public DeploymentResult deploy(DeploymentRequest request) {
        Instant start=Instant.now();DeploymentCellManifest manifest=request.manifest();
        DeploymentResult invalid=validate(request,start); if(invalid!=null)return invalid;
        Optional<Map<String,Object>> existing=executions.begin(request);
        if(existing.isPresent()) return fromExisting(request,existing.get(),start);
        DeploymentResult lockFailure=acquire(request,start);if(lockFailure!=null)return lockFailure;
        List<DeploymentResult.InstanceResult> results=new ArrayList<>();int sequence=0;
        try {
            List<DeploymentCellManifest.Instance> order=ordered(manifest);
            int healthy=currentHealthy(manifest.serviceId());
            for(DeploymentCellManifest.Instance instance:order){
                if(healthy-1<manifest.deployment().minHealthy())
                    return rollbackAfterFailure(request,results,start,"MIN_HEALTHY",sequence);
                DeploymentTargetAdapter adapter=adapter(manifest,instance);
                var drain=adapter.drain(manifest,instance);record(request,++sequence,drain,results);if(!success(drain))return rollbackAfterFailure(request,results,start,"DRAIN",sequence);
                healthy=Math.max(0,healthy-1);
                var deployed=adapter.deploy(manifest,instance);record(request,++sequence,deployed,results);if(!success(deployed))return rollbackAfterFailure(request,results,start,"INSTALL",sequence);
                var ready=health.probe(instance,manifest.deployment().healthPath(),manifest.deployment().healthTimeoutSeconds());
                if(!ready.ready()){var failed=new DeploymentResult.InstanceResult(instance.instanceId(),CommandState.FAILED,"READINESS",ready.detail());record(request,++sequence,failed,results);return rollbackAfterFailure(request,results,start,"READINESS",sequence);}
                if(manifest.deployment().functionalSmokeRequired()){
                    var smoke=health.smoke(instance,manifest.deployment().healthTimeoutSeconds());
                    if(!smoke.ready()){var failed=new DeploymentResult.InstanceResult(instance.instanceId(),CommandState.FAILED,"FUNCTIONAL_SMOKE",smoke.detail());record(request,++sequence,failed,results);return rollbackAfterFailure(request,results,start,"FUNCTIONAL_SMOKE",sequence);}
                }
                var resume=adapter.resume(manifest,instance);record(request,++sequence,resume,results);if(!success(resume))return rollbackAfterFailure(request,results,start,"ADMISSION",sequence);
                healthy++;
            }
            return finish(request,CommandState.SUCCEEDED,null,"Deployment completed",results,start);
        } catch(RuntimeException e){
            return rollbackAfterFailure(request,results,start,"UNEXPECTED",sequence,
                    "Unexpected deployment failure ("+e.getClass().getSimpleName()+"): "
                            +SensitiveTextSanitizer.sanitize(e.getMessage()));
        } finally { cellLock.release(manifest.cellId(),request.deploymentId()); }
    }

    public DeploymentResult rollbackApproved(DeploymentRequest request) {
        Instant start=Instant.now();DeploymentResult invalid=validateApproval(request,start);if(invalid!=null)return invalid;
        Optional<Map<String,Object>> existing=executions.begin(request);if(existing.isPresent())return fromExisting(request,existing.get(),start);
        DeploymentResult lockFailure=acquire(request,start);if(lockFailure!=null)return lockFailure;
        List<DeploymentResult.InstanceResult> results=new ArrayList<>();int seq=0;boolean failed=false;
        try {
            for(var instance:request.manifest().instances()){
                DeploymentTargetAdapter adapter=adapter(request.manifest(),instance);
                var drain=adapter.drain(request.manifest(),instance);record(request,++seq,drain,results);failed|=!success(drain);if(!success(drain))continue;
                var rb=adapter.rollback(request.manifest(),instance);record(request,++seq,rb,results);failed|=!success(rb);if(!success(rb))continue;
                var ready=health.probe(instance,request.manifest().deployment().healthPath(),request.manifest().deployment().healthTimeoutSeconds());
                if(!ready.ready()){var x=new DeploymentResult.InstanceResult(instance.instanceId(),CommandState.FAILED,"ROLLBACK_READINESS",ready.detail());record(request,++seq,x,results);failed=true;continue;}
                var resume=adapter.resume(request.manifest(),instance);record(request,++seq,resume,results);failed|=!success(resume);
            }
            return finish(request,failed?CommandState.PARTIALLY_ROLLED_BACK:CommandState.ROLLED_BACK,
                    failed?"ROLLBACK_PARTIAL":null,failed?"Rollback incomplete":"Rollback completed",results,start);
        } finally {cellLock.release(request.manifest().cellId(),request.deploymentId());}
    }

    private DeploymentResult validate(DeploymentRequest r,Instant start){
        DeploymentResult approval=validateApproval(r,start);if(approval!=null)return approval;
        var result=compatibility.evaluate(r.manifest().artifact(),r.manifest().environment());
        if(!result.allowed())return new DeploymentResult(r.deploymentId(),CommandState.FAILED,"CAN_DEPLOY",result.reason(),null,r.manifest().artifact().version(),List.of(),start,Instant.now());
        return null;
    }
    private DeploymentResult validateApproval(DeploymentRequest r,Instant start){
        if(r.requestedBy()==null||r.requestedBy().isBlank()||r.approvedBy()==null||r.approvedBy().isBlank()||r.requestedBy().equals(r.approvedBy())||r.reason()==null||r.reason().isBlank())
            return new DeploymentResult(r.deploymentId(),CommandState.FAILED,"APPROVAL","Requester/approver separation and reason are required",null,r.manifest().artifact().version(),List.of(),start,Instant.now());
        if(r.expiresAt()!=null&&r.expiresAt().isBefore(Instant.now()))
            return new DeploymentResult(r.deploymentId(),CommandState.FAILED,"EXPIRY","Approval expired",null,r.manifest().artifact().version(),List.of(),start,Instant.now());
        return null;
    }
    private List<DeploymentCellManifest.Instance> ordered(DeploymentCellManifest m){
        if(m.deployment().strategy()==DeploymentStrategy.CANARY && m.instances().size()>1){
            List<DeploymentCellManifest.Instance> list=new ArrayList<>();list.add(m.instances().getFirst());list.addAll(m.instances().subList(1,m.instances().size()));return list;
        }
        return m.instances();
    }
    private DeploymentResult rollbackAfterFailure(DeploymentRequest r,List<DeploymentResult.InstanceResult> results,Instant start,String stage,int seq){
        return rollbackAfterFailure(r,results,start,stage,seq,null);
    }
    private DeploymentResult rollbackAfterFailure(DeploymentRequest r,List<DeploymentResult.InstanceResult> results,Instant start,String stage,int seq,String failureDetail){
        boolean rollbackFailed=false;int sequence=seq;
        for(var instance:r.manifest().instances()){
            try{var rb=adapter(r.manifest(),instance).rollback(r.manifest(),instance);record(r,++sequence,rb,results);rollbackFailed|=!success(rb);}catch(RuntimeException e){var x=new DeploymentResult.InstanceResult(instance.instanceId(),CommandState.UNKNOWN_RESULT,"ROLLBACK",e.getClass().getSimpleName());record(r,++sequence,x,results);rollbackFailed=true;}
        }
        return finish(r,rollbackFailed?CommandState.PARTIALLY_ROLLED_BACK:CommandState.ROLLED_BACK,stage,
                failureDetail==null
                        ? (rollbackFailed?"Deployment failed; rollback result incomplete":"Deployment failed; rollback completed")
                        : failureDetail+(rollbackFailed?"; rollback result incomplete":"; rollback completed"),
                results,start);
    }
    private void record(DeploymentRequest r,int sequence,DeploymentResult.InstanceResult result,List<DeploymentResult.InstanceResult> all){all.add(result);executions.instance(r.deploymentId(),sequence,result);}
    private DeploymentResult finish(DeploymentRequest r,CommandState state,String stage,String message,List<DeploymentResult.InstanceResult> out,Instant start){executions.finish(r.deploymentId(),state,stage,message);return new DeploymentResult(r.deploymentId(),state,stage,message,null,r.manifest().artifact().version(),List.copyOf(out),start,Instant.now());}
    private DeploymentResult fromExisting(DeploymentRequest r,Map<String,Object> existing,Instant start){CommandState state=CommandState.valueOf(Objects.toString(existing.get("execution_state"),"UNKNOWN_RESULT"));return new DeploymentResult(r.deploymentId(),state,Objects.toString(existing.get("failure_stage"),null),Objects.toString(existing.get("result_message"),"Existing idempotent deployment"),Objects.toString(existing.get("from_version"),null),Objects.toString(existing.get("to_version"),r.manifest().artifact().version()),List.of(),start,Instant.now());}
    private DeploymentTargetAdapter adapter(DeploymentCellManifest m,DeploymentCellManifest.Instance i){return adapters.stream().filter(a->a.supports(i,m.runtimeMode())).findFirst().orElseThrow(()->new IllegalStateException("No deployment adapter for "+i.instanceId()));}
    private boolean success(DeploymentResult.InstanceResult r){return r.state()==CommandState.SUCCEEDED;}
    private int currentHealthy(String service){Integer n=jdbc.queryForObject(sql.required("deploy-runtime-healthy-count"),Integer.class,service);return n==null?0:n;}
    private DeploymentResult acquire(DeploymentRequest request,Instant start){
        try{
            if(cellLock.acquire(request.manifest().cellId(),request.deploymentId())==DeploymentCellLock.Acquisition.ACQUIRED)return null;
            return finish(request,CommandState.FAILED,"DEPLOYMENT_LOCK","Cell is already locked",List.of(),start);
        }catch(RuntimeException failure){
            return finish(request,CommandState.UNKNOWN_RESULT,"DEPLOYMENT_LOCK_STORE",
                    "Deployment lock store is unavailable: "+failure.getClass().getSimpleName(),List.of(),start);
        }
    }
}
