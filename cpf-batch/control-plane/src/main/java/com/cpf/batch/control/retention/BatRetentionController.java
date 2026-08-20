package com.cpf.batch.control.retention;

import com.cpf.batch.control.security.BatVerifiedActorResolver;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalTime;
import java.util.Map;

/** Retention 운영 Control Plane API. Business Transaction annotation을 사용하지 않습니다. */
@RestController
@RequestMapping("/bat/api/retention")
public class BatRetentionController {
    private final BatRetentionOperations operations;
    private final BatRetentionExecutionService execution;
    private final Environment environment;
    private final BatVerifiedActorResolver actorResolver;

    public BatRetentionController(BatRetentionOperations operations, BatRetentionExecutionService execution,
                                  Environment environment, BatVerifiedActorResolver actorResolver) {
        this.operations = operations;
        this.execution = execution;
        this.environment = environment;
        this.actorResolver = actorResolver;
    }

    @GetMapping("/targets") public ResponseEntity<?> targets(){return ResponseEntity.ok(operations.targets());}
    @GetMapping("/policies") public ResponseEntity<?> policies(){return ResponseEntity.ok(execution.policies());}
    @GetMapping("/runs") public ResponseEntity<?> runs(@RequestParam(required=false) String policyId,@RequestParam(defaultValue="100") int limit){return ResponseEntity.ok(execution.runs(policyId,limit));}
    @GetMapping("/audit/by-approval/{approvalRequestId}")
    public ResponseEntity<?> auditByApproval(@PathVariable String approvalRequestId,HttpServletRequest http){
        actorResolver.actor(http,null,"requestedBy");
        return ResponseEntity.ok(execution.audits(approvalRequestId));
    }

    @PostMapping("/policies")
    public ResponseEntity<?> savePolicy(@RequestBody PolicyRequest r,HttpServletRequest http){
        var approved=approved(http,r.requestedBy(),r.approvedBy(),r.approvalRequestId());
        return ResponseEntity.ok(execution.savePolicy(r.toDefinition(),approved.requestedBy(),approved.approvedBy(),approved.approvalRequestId(),reason(r.reason())));
    }

    @PostMapping("/policies/{policyId}/pause")
    public ResponseEntity<?> pausePolicy(@PathVariable String policyId,@RequestBody PauseRequest r,HttpServletRequest http){
        String actor=actorResolver.actor(http,null,"requestedBy");
        return ResponseEntity.ok(execution.pausePolicy(policyId,true,r.expectedVersion(),actor,reason(r.reason()),null,null));
    }

    @PostMapping("/policies/{policyId}/resume")
    public ResponseEntity<?> resumePolicy(@PathVariable String policyId,@RequestBody RunRequest r,HttpServletRequest http){
        var approved=approved(http,r.requestedBy(),r.approvedBy(),r.approvalRequestId());
        return ResponseEntity.ok(execution.pausePolicy(policyId,false,r.expectedVersion(),approved.requestedBy(),reason(r.reason()),approved.approvedBy(),approved.approvalRequestId()));
    }

    @PostMapping("/policies/{policyId}/run")
    public ResponseEntity<?> runNow(@PathVariable String policyId,@RequestBody RunRequest r,HttpServletRequest http){
        if(!environment.getProperty("cpf.retention.execute-enabled",Boolean.class,false))return ResponseEntity.status(403).body(Map.of("message","cpf.retention.execute-enabled=true일 때만 실행할 수 있습니다."));
        var approved=approved(http,r.requestedBy(),r.approvedBy(),r.approvalRequestId());
        return ResponseEntity.ok(execution.runNow(policyId,r.expectedVersion(),approved.requestedBy(),approved.approvedBy(),approved.approvalRequestId(),reason(r.reason())));
    }

    @PostMapping("/runs/{runId}/pause")
    public ResponseEntity<?> pauseRun(@PathVariable String runId,@RequestBody PauseRequest r,HttpServletRequest http){
        String actor=actorResolver.actor(http,null,"requestedBy");
        return ResponseEntity.ok(execution.requestPause(runId,actor,reason(r.reason())));
    }

    @PostMapping("/runs/{runId}/resume")
    public ResponseEntity<?> resumeRun(@PathVariable String runId,@RequestBody RunRequest r,HttpServletRequest http){
        if(!environment.getProperty("cpf.retention.execute-enabled",Boolean.class,false))return ResponseEntity.status(403).body(Map.of("message","cpf.retention.execute-enabled=true일 때만 실행할 수 있습니다."));
        var approved=approved(http,r.requestedBy(),r.approvedBy(),r.approvalRequestId());
        return ResponseEntity.ok(execution.resume(runId,r.expectedVersion(),approved.requestedBy(),approved.approvedBy(),approved.approvalRequestId(),reason(r.reason())));
    }

    /** 기존 ad-hoc Preview는 호환용으로 유지하되 실제 파괴 실행은 정책 기반 Engine만 허용합니다. */
    @PostMapping("/preview") public ResponseEntity<?> preview(@RequestBody LegacyPreviewRequest r,HttpServletRequest http){
        String actor=actorResolver.actor(http,r.requestedBy(),"requestedBy"); String action=r.action()==null?"KEEP":r.action().trim().toUpperCase();
        var result=operations.execute(new com.cpf.platform.operations.api.retention.CpfRetentionCommand(new com.cpf.platform.operations.api.retention.CpfRetentionPolicy(r.target(),action,r.legalHold(),true),r.cutoff(),actor,reason(r.reason()),Math.max(1,Math.min(r.limit(),100000))));
        return ResponseEntity.ok(result);
    }

    private BatVerifiedActorResolver.ApprovedActors approved(HttpServletRequest request,String requestedBy,String approvedBy,String approvalRequestId){
        return actorResolver.approved(request,requestedBy,approvedBy,approvalRequestId);
    }
    private static String reason(String v){if(v==null||v.isBlank())throw new IllegalArgumentException("실행 사유는 필수입니다.");return v.trim();}

    public record RunRequest(long expectedVersion,String reason,String requestedBy,String approvedBy,String approvalRequestId){}
    public record PauseRequest(long expectedVersion,String reason){}
    public record LegacyPreviewRequest(String target,String action,Instant cutoff,boolean legalHold,String reason,int limit,String requestedBy){}
    public record PolicyRequest(String policyId,String target,String action,int retentionDays,String scheduleExpression,String maintenanceStart,String maintenanceEnd,boolean enabled,boolean legalHold,int chunkSize,long throttleMillis,long maxRowsPerRun,long maxRuntimeSeconds,int leaseSeconds,long policyVersion,Instant nextRunAt,long rowVersion,String reason,String requestedBy,String approvedBy,String approvalRequestId){
        BatRetentionPolicyDefinition toDefinition(){return new BatRetentionPolicyDefinition(policyId,target,action,retentionDays,scheduleExpression,parseTime(maintenanceStart),parseTime(maintenanceEnd),enabled,legalHold,chunkSize,throttleMillis,maxRowsPerRun,maxRuntimeSeconds,leaseSeconds,policyVersion,nextRunAt,rowVersion);}
        private static LocalTime parseTime(String v){return v==null||v.isBlank()?null:LocalTime.parse(v);}
    }
}
