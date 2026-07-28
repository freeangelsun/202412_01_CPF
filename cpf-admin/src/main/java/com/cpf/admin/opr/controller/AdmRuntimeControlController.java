package com.cpf.admin.opr.controller;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.core.api.logging.CpfTransactionContext;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.runtimecontrol.*;
import com.cpf.core.common.runtimecontrol.CpfRuntimeFenceException;
import com.cpf.core.common.runtimecontrol.CpfRuntimeRateLimitException;
import com.cpf.core.common.runtimecontrol.CpfRuntimeVersionConflictException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/** ADM Runtime Control Plane 운영/Agent API입니다. 운영 API와 Agent API의 신뢰경계를 분리합니다. */
@RestController
@Tag(name="ADM-RuntimeControl",description="Runtime 변경 계획, 배포, ACK, drift 및 Agent lease/fencing 제어")
public class AdmRuntimeControlController extends com.cpf.admin.common.base.AdmBaseController {
    private static final String TOKEN_HEADER="X-Cpf-Runtime-Agent-Token";
    private final CpfRuntimeControlPlane controlPlane;
    private final AdmAuditLogService audit;
    private final String agentToken;

    public AdmRuntimeControlController(CpfRuntimeControlPlane controlPlane,AdmAuditLogService audit,
            @Value("${cpf.runtime.control.agent-token:${CPF_RUNTIME_CONTROL_AGENT_TOKEN:}}") String agentToken){
        this.controlPlane=controlPlane;this.audit=audit;this.agentToken=agentToken==null?"":agentToken;
    }

    @PostMapping("/adm/api/runtime-control/changes")
    @CpfOnlineTransaction(id="OADMRC0010",name="ADMRuntimeChangeCreate")
    @Operation(summary="Runtime 변경 생성",description="operationId fingerprint/CAS/대상 snapshot/durable delivery를 원자적으로 생성합니다.")
    public ResponseEntity<CpfRuntimeChangeResult> create(@RequestBody CpfRuntimeChangeCommand command,HttpServletRequest request){
        String operator=operator(request);requireCommandOwner(command,operator);requireRiskApproval(command);
        CpfRuntimeChangeResult result=controlPlane.createChange(command);
        audit(request,operator,"RUNTIME_CHANGE_CREATE",result.changeId(),command.reason(),Map.of("changeType",command.changeType(),"state",result.state(),"requestHash",result.requestHash()));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/adm/api/runtime-control/changes/{changeId}")
    @CpfOnlineTransaction(id="OADMRC0020",name="ADMRuntimeChangeDetail")
    public ResponseEntity<CpfRuntimeChangeResult> get(@PathVariable String changeId,HttpServletRequest request){operator(request);return ResponseEntity.ok(controlPlane.getChange(changeId));}

    @GetMapping("/adm/api/runtime-control/operations/{operationId}")
    @CpfOnlineTransaction(id="OADMRC0030",name="ADMRuntimeOperationRecovery")
    public ResponseEntity<CpfRuntimeChangeResult> byOperation(@PathVariable String operationId,HttpServletRequest request){operator(request);return ResponseEntity.ok(controlPlane.getByOperationId(operationId));}

    @GetMapping("/adm/api/runtime-control/status")
    @CpfOnlineTransaction(id="OADMRC0040",name="ADMRuntimeStatus")
    public ResponseEntity<Map<String,Object>> status(@RequestParam(required=false)String environment,@RequestParam(required=false)String serviceId,HttpServletRequest request){operator(request);return ResponseEntity.ok(controlPlane.status(environment,serviceId));}

    @GetMapping("/adm/api/runtime-control/health")
    @CpfOnlineTransaction(id="OADMRC0050",name="ADMRuntimeHealth")
    public ResponseEntity<CpfRuntimeControlHealth> health(HttpServletRequest request){operator(request);return ResponseEntity.ok(controlPlane.health());}

    @GetMapping("/adm/api/runtime-control/states")
    @CpfOnlineTransaction(id="OADMRC0060",name="ADMRuntimeStateCatalog")
    public ResponseEntity<Map<String,Object>> states(HttpServletRequest request){
        operator(request);
        return ResponseEntity.ok(Map.of(
                "ack",CpfRuntimeStateCatalog.ackStates(),
                "delivery",CpfRuntimeStateCatalog.deliveryStates(),
                "change",CpfRuntimeStateCatalog.changeStates(),
                "drift",CpfRuntimeStateCatalog.driftStates()));
    }

    @PostMapping("/adm/api/runtime-control/preview-targets")
    @CpfOnlineTransaction(id="OADMRC0070",name="ADMRuntimeTargetPreview")
    public ResponseEntity<Map<String,Object>> previewTargets(@RequestBody PreviewTargetRequest body,HttpServletRequest request){
        operator(request);return ResponseEntity.ok(controlPlane.previewTargets(body.changeType(),body.payloadSchemaVersion(),body.target()));
    }

    @PostMapping("/adm/api/runtime-control/preview-change")
    @CpfOnlineTransaction(id="OADMRC0080",name="ADMRuntimeChangePreview")
    public ResponseEntity<Map<String,Object>> previewChange(@RequestBody CpfRuntimeChangeCommand command,HttpServletRequest request){
        String operator=operator(request);requireCommandOwner(command,operator);return ResponseEntity.ok(controlPlane.previewChange(command));
    }

    @GetMapping("/adm/api/runtime-control/changes/{changeId}/audit/verify")
    @CpfOnlineTransaction(id="OADMRC0090",name="ADMRuntimeAuditVerify")
    public ResponseEntity<CpfRuntimeAuditVerification> verifyAudit(@PathVariable String changeId,HttpServletRequest request){
        operator(request);CpfRuntimeAuditVerification result=controlPlane.verifyAudit(changeId);
        return result.valid()?ResponseEntity.ok(result):ResponseEntity.status(HttpStatus.CONFLICT).body(result);
    }

    @PostMapping("/adm/api/runtime-control/changes/{changeId}/cancel")
    @CpfOnlineTransaction(id="OADMRC0100",name="ADMRuntimeChangeCancel")
    public ResponseEntity<CpfRuntimeChangeResult> cancel(@PathVariable String changeId,@RequestBody ControlRequest body,HttpServletRequest request){
        String operator=operator(request); CpfRuntimeChangeResult result=controlPlane.cancel(changeId,body.operationId(),body.reason(),operator);
        audit(request,operator,"RUNTIME_CHANGE_CANCEL",changeId,body.reason(),Map.of("state",result.state()));return ResponseEntity.ok(result);
    }

    @PostMapping("/adm/api/runtime-control/changes/{changeId}/rollback")
    @CpfOnlineTransaction(id="OADMRC0110",name="ADMRuntimeChangeRollback")
    public ResponseEntity<CpfRuntimeChangeResult> rollback(@PathVariable String changeId,@RequestBody ControlRequest body,HttpServletRequest request){
        String operator=operator(request); CpfRuntimeChangeResult result=controlPlane.rollback(changeId,body.operationId(),body.reason(),operator);
        audit(request,operator,"RUNTIME_CHANGE_ROLLBACK",changeId,body.reason(),Map.of("rollbackChangeId",result.changeId(),"state",result.state()));return ResponseEntity.ok(result);
    }

    @PostMapping("/adm/api/runtime-control/groups")
    @CpfOnlineTransaction(id="OADMRC0120",name="ADMRuntimeGroupSave")
    public ResponseEntity<CpfRuntimeGroupResult> saveGroup(@RequestBody CpfRuntimeGroupCommand command,HttpServletRequest request){String operator=operator(request);if(command.requestedBy()==null||!operator.equals(command.requestedBy()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"requestedBy mismatch");CpfRuntimeGroupResult result=controlPlane.saveGroup(command);audit(request,operator,"RUNTIME_GROUP_SAVE",result.groupId(),command.reason(),result);return ResponseEntity.ok(result);}

    @GetMapping("/adm/api/runtime-control/groups/{groupId}")
    @CpfOnlineTransaction(id="OADMRC0130",name="ADMRuntimeGroupDetail")
    public ResponseEntity<CpfRuntimeGroupResult> getGroup(@PathVariable String groupId,HttpServletRequest request){operator(request);return ResponseEntity.ok(controlPlane.getGroup(groupId));}

    @PostMapping("/adm/api/runtime-control/groups/{groupId}/members")
    @CpfOnlineTransaction(id="OADMRC0140",name="ADMRuntimeGroupMember")
    public ResponseEntity<CpfRuntimeGroupResult> groupMember(@PathVariable String groupId,@RequestBody CpfRuntimeGroupMemberCommand command,HttpServletRequest request){String operator=operator(request);if(!groupId.equals(command.groupId()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"groupId mismatch");if(command.requestedBy()==null||!operator.equals(command.requestedBy()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"requestedBy mismatch");CpfRuntimeGroupResult result=controlPlane.changeGroupMember(command);audit(request,operator,"RUNTIME_GROUP_MEMBER",groupId,command.reason(),result);return ResponseEntity.ok(result);}

    @DeleteMapping("/adm/api/runtime-control/groups/{groupId}")
    @CpfOnlineTransaction(id="OADMRC0150",name="ADMRuntimeGroupDelete")
    public ResponseEntity<Void> deleteGroup(@PathVariable String groupId,@RequestParam String operationId,@RequestParam long expectedVersion,@RequestParam String reason,HttpServletRequest request){String operator=operator(request);controlPlane.deleteGroup(groupId,operationId,expectedVersion,reason,operator);audit(request,operator,"RUNTIME_GROUP_DELETE",groupId,reason,Map.of("deleted",true));return ResponseEntity.noContent().build();}

    @PostMapping("/cpf/runtime-control/agent/register") @Operation(summary="Runtime Agent 자기등록")
    public ResponseEntity<CpfRuntimeInstanceLease> register(@RequestHeader(TOKEN_HEADER)String token,@RequestBody CpfRuntimeInstanceRegistration registration){agent(token);return ResponseEntity.ok(controlPlane.register(registration));}

    @PostMapping("/cpf/runtime-control/agent/heartbeat") @Operation(summary="Runtime Agent lease heartbeat")
    public ResponseEntity<CpfRuntimeInstanceLease> heartbeat(@RequestHeader(TOKEN_HEADER)String token,@RequestBody HeartbeatRequest body){
        agent(token);
        return ResponseEntity.ok(controlPlane.heartbeat(body.instanceId(),body.fencingToken(),body.actualHash(),
                body.actualVersion(),body.agentTime()==null?Instant.now():body.agentTime()));
    }

    @PostMapping("/cpf/runtime-control/agent/deregister") @Operation(summary="Runtime Agent graceful deregistration")
    public ResponseEntity<Void> deregister(@RequestHeader(TOKEN_HEADER)String token,@RequestBody DeregisterRequest body){
        agent(token);controlPlane.deregister(body.instanceId(),body.fencingToken(),body.reason());return ResponseEntity.noContent().build();
    }

    @PostMapping("/cpf/runtime-control/agent/actual-state") @Operation(summary="Runtime Agent durable actual state 재보고")
    public ResponseEntity<Void> actualState(@RequestHeader(TOKEN_HEADER)String token,@RequestBody ActualStateRequest body){
        agent(token);controlPlane.reconcileActualState(body.instanceId(),body.fencingToken(),body.states());return ResponseEntity.noContent().build();
    }

    @PostMapping("/cpf/runtime-control/agent/claim") @Operation(summary="Runtime Agent durable delivery claim")
    public ResponseEntity<List<CpfRuntimeDelivery>> claim(@RequestHeader(TOKEN_HEADER)String token,@RequestBody ClaimRequest body){agent(token);return ResponseEntity.ok(controlPlane.claim(body.instanceId(),body.fencingToken(),body.limit()));}

    @PostMapping("/cpf/runtime-control/agent/ack") @Operation(summary="Runtime Agent ACK")
    public ResponseEntity<CpfRuntimeChangeResult> ack(@RequestHeader(TOKEN_HEADER)String token,@RequestBody CpfRuntimeAck ack){agent(token);return ResponseEntity.ok(controlPlane.acknowledge(ack));}

    @ExceptionHandler(CpfRuntimeRateLimitException.class)
    public ResponseEntity<Map<String,Object>> rateLimit(CpfRuntimeRateLimitException ex){
        return error(HttpStatus.TOO_MANY_REQUESTS,"CPF_RUNTIME_RATE_LIMIT",ex.getMessage());
    }

    @ExceptionHandler({CpfRuntimeVersionConflictException.class,CpfRuntimeFenceException.class})
    public ResponseEntity<Map<String,Object>> conflict(RuntimeException ex){
        return error(HttpStatus.CONFLICT,"CPF_RUNTIME_CONFLICT",ex.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<Map<String,Object>> unavailable(DataAccessException ex){
        return error(HttpStatus.SERVICE_UNAVAILABLE,"CPF_RUNTIME_CONTROL_STORE_UNAVAILABLE",
                "Runtime Control Store를 사용할 수 없습니다.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String,Object>> badRequest(IllegalArgumentException ex){
        return error(HttpStatus.BAD_REQUEST,"CPF_RUNTIME_BAD_REQUEST",ex.getMessage());
    }

    private ResponseEntity<Map<String,Object>> error(HttpStatus status,String code,String message){
        return ResponseEntity.status(status).body(Map.of(
                "code",code,
                "message",message==null?"Runtime Control 요청 처리 실패":message,
                "timestamp",Instant.now().toString()));
    }

    private void requireCommandOwner(CpfRuntimeChangeCommand c,String operator){if(c.requestedBy()==null||!operator.equals(c.requestedBy()))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"requestedBy는 인증된 ADM 운영자와 일치해야 합니다.");}
    private void requireRiskApproval(CpfRuntimeChangeCommand c){
        String type=c.changeType()==null?"":c.changeType().toUpperCase();
        boolean dangerous=type.contains("ROUTE")||type.contains("SHUTDOWN")||type.contains("DRAIN")||type.contains("SECURITY")||type.contains("SECRET")||type.contains("PERMISSION")||type.contains("BATCH_CONTROL");
        if(dangerous && blank(c.approvalId()) && blank(c.breakGlassId()))throw new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED,"위험 Runtime 변경에는 approvalId 또는 승인된 breakGlassId가 필요합니다.");
    }
    private boolean blank(String v){return v==null||v.isBlank();}
    private String operator(HttpServletRequest request){Object value=request.getAttribute("adm.operatorId");if(value instanceof String s&&!s.isBlank())return s;throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"검증된 ADM operator session이 필요합니다.");}
    private void agent(String provided){
        if(agentToken.isBlank())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Runtime Agent credential이 구성되지 않았습니다.");
        byte[] a=agentToken.getBytes(StandardCharsets.UTF_8),b=(provided==null?"":provided).getBytes(StandardCharsets.UTF_8);
        if(!MessageDigest.isEqual(a,b))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Runtime Agent 인증에 실패했습니다.");
    }
    private void audit(HttpServletRequest req,String user,String action,String id,String reason,Object after){
        audit.record(CpfTransactionContext.transactionId(),user,action,"cpf_runtime_change",id,reason,"",String.valueOf(after),"Runtime Control Plane",req.getRemoteAddr());
    }

    public record ControlRequest(String operationId,String reason){}
    public record PreviewTargetRequest(String changeType,int payloadSchemaVersion,CpfRuntimeTargetSelector target){}
    public record HeartbeatRequest(String instanceId,long fencingToken,String actualHash,long actualVersion,Instant agentTime){}
    public record DeregisterRequest(String instanceId,long fencingToken,String reason){}
    public record ActualStateRequest(String instanceId,long fencingToken,List<CpfRuntimeActualState> states){}
    public record ClaimRequest(String instanceId,long fencingToken,int limit){}
}
