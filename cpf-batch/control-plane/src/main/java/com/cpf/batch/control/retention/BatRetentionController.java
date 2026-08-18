package com.cpf.batch.control.retention;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Map;

/** Retention 운영 Control Plane API. Business Transaction annotation을 사용하지 않습니다. */
@RestController
@RequestMapping("/bat/api/retention")
public class BatRetentionController {
    private final BatRetentionOperations operations; private final BatRetentionExecutionService execution; private final Environment environment;
    public BatRetentionController(BatRetentionOperations operations,BatRetentionExecutionService execution,Environment environment){this.operations=operations;this.execution=execution;this.environment=environment;}

    @GetMapping("/targets") public ResponseEntity<?> targets(){return ResponseEntity.ok(operations.targets());}
    @GetMapping("/policies") public ResponseEntity<?> policies(){return ResponseEntity.ok(execution.policies());}
    @GetMapping("/runs") public ResponseEntity<?> runs(@RequestParam(required=false) String policyId,@RequestParam(defaultValue="100") int limit){return ResponseEntity.ok(execution.runs(policyId,limit));}

    @PostMapping("/policies")
    public ResponseEntity<?> savePolicy(@RequestBody PolicyRequest r,HttpServletRequest http){String actor=actor(http);return ResponseEntity.ok(execution.savePolicy(r.toDefinition(),actor));}
    @PostMapping("/policies/{policyId}/pause") public ResponseEntity<?> pausePolicy(@PathVariable String policyId,HttpServletRequest http){return ResponseEntity.ok(execution.pausePolicy(policyId,true,actor(http)));}
    @PostMapping("/policies/{policyId}/resume") public ResponseEntity<?> resumePolicy(@PathVariable String policyId,HttpServletRequest http){return ResponseEntity.ok(execution.pausePolicy(policyId,false,actor(http)));}

    @PostMapping("/policies/{policyId}/run")
    public ResponseEntity<?> runNow(@PathVariable String policyId,@RequestBody RunRequest r,HttpServletRequest http){
        if(!environment.getProperty("cpf.retention.execute-enabled",Boolean.class,false))return ResponseEntity.status(403).body(Map.of("message","cpf.retention.execute-enabled=true일 때만 실행할 수 있습니다."));
        return ResponseEntity.ok(execution.runNow(policyId,actor(http),reason(r.reason())));
    }
    @PostMapping("/runs/{runId}/pause") public ResponseEntity<?> pauseRun(@PathVariable String runId,HttpServletRequest http){return ResponseEntity.ok(execution.requestPause(runId,actor(http)));}
    @PostMapping("/runs/{runId}/resume") public ResponseEntity<?> resumeRun(@PathVariable String runId,@RequestBody RunRequest r,HttpServletRequest http){
        if(!environment.getProperty("cpf.retention.execute-enabled",Boolean.class,false))return ResponseEntity.status(403).body(Map.of("message","cpf.retention.execute-enabled=true일 때만 실행할 수 있습니다."));
        return ResponseEntity.ok(execution.resume(runId,actor(http),reason(r.reason())));
    }

    /** 기존 ad-hoc Preview는 호환용으로 유지하되 실제 파괴 실행은 정책 기반 Engine만 허용합니다. */
    @PostMapping("/preview") public ResponseEntity<?> preview(@RequestBody LegacyPreviewRequest r,HttpServletRequest http){
        String actor=actor(http); String action=r.action()==null?"KEEP":r.action().trim().toUpperCase();
        var result=operations.execute(new com.cpf.platform.operations.api.retention.CpfRetentionCommand(new com.cpf.platform.operations.api.retention.CpfRetentionPolicy(r.target(),action,r.legalHold(),true),r.cutoff(),actor,reason(r.reason()),Math.max(1,Math.min(r.limit(),100000))));
        return ResponseEntity.ok(result);
    }
    private static String actor(HttpServletRequest h){Principal p=h.getUserPrincipal();if(p==null||p.getName()==null||p.getName().isBlank())throw new SecurityException("인증 사용자가 필요합니다.");return p.getName();}
    private static String reason(String v){if(v==null||v.isBlank())throw new IllegalArgumentException("실행 사유는 필수입니다.");return v.trim();}

    public record RunRequest(String reason){}
    public record LegacyPreviewRequest(String target,String action,Instant cutoff,boolean legalHold,String reason,int limit){}
    public record PolicyRequest(String policyId,String target,String action,int retentionDays,String scheduleExpression,String maintenanceStart,String maintenanceEnd,boolean enabled,boolean legalHold,int chunkSize,long throttleMillis,long maxRowsPerRun,long maxRuntimeSeconds,int leaseSeconds,long policyVersion,Instant nextRunAt,long rowVersion){
        BatRetentionPolicyDefinition toDefinition(){return new BatRetentionPolicyDefinition(policyId,target,action,retentionDays,scheduleExpression,parseTime(maintenanceStart),parseTime(maintenanceEnd),enabled,legalHold,chunkSize,throttleMillis,maxRowsPerRun,maxRuntimeSeconds,leaseSeconds,policyVersion,nextRunAt,rowVersion);}
        private static LocalTime parseTime(String v){return v==null||v.isBlank()?null:LocalTime.parse(v);}
    }
}
