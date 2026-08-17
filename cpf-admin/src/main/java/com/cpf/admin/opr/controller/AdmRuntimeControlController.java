package com.cpf.admin.opr.controller;

import org.springframework.web.bind.annotation.RestController;
import com.cpf.admin.opr.dto.AdmApiErrorResponse;
import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.platform.operations.runtimecontrol.*;
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

    @PostMapping("/adm/api/runtime-control/changes")    @Operation(operationId="admRuntimeControlCreateChange", summary="Runtime 변경 생성",description="operationId fingerprint/CAS/대상 snapshot/durable delivery를 원자적으로 생성합니다.")
    public ResponseEntity<CpfRuntimeChangeResult> create(@RequestBody RuntimeChangeRequest body,HttpServletRequest request){
        String operator=operator(request);
        CpfRuntimeChangeCommand command=body.toCommand(operator);
        requireRiskApproval(command);
        CpfRuntimeChangeResult result=controlPlane.createChange(command);
        audit(request,operator,"RUNTIME_CHANGE_CREATE",result.changeId(),command.reason(),Map.of("changeType",command.changeType(),"capability",CpfRuntimeCapabilityCatalog.describe(command
                .changeType()),"state",result.state(),"requestHash",result.requestHash()));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/adm/api/runtime-control/changes/{changeId}")    @Operation(operationId="admRuntimeControlFindChange", summary="Runtime 변경 상세 조회")
    public ResponseEntity<CpfRuntimeChangeResult> get(@PathVariable String changeId,HttpServletRequest request){operator(request);return ResponseEntity.ok(controlPlane.getChange(changeId));}

    @GetMapping("/adm/api/runtime-control/operations/{operationId}")    @Operation(operationId="admRuntimeControlFindByOperation", summary="Operation ID 결과 복구 조회")
    public ResponseEntity<CpfRuntimeChangeResult> byOperation(@PathVariable String operationId,HttpServletRequest request){operator(request);return ResponseEntity.ok(controlPlane.getByOperationId(operationId));}

    @GetMapping("/adm/api/runtime-control/status")    @Operation(operationId="admRuntimeControlFindStatus", summary="Runtime 상태 조회")
    public ResponseEntity<CpfRuntimeStatus> status(@RequestParam(required=false)String environment,@RequestParam(required=false)String serviceId,HttpServletRequest request){operator(request);return
            ResponseEntity.ok(controlPlane.status(environment,serviceId));}

    @GetMapping("/adm/api/runtime-control/health")    @Operation(operationId="admRuntimeControlFindHealth", summary="Runtime Control Health 조회")
    public ResponseEntity<CpfRuntimeControlHealth> health(HttpServletRequest request){operator(request);return ResponseEntity.ok(controlPlane.health());}

    @GetMapping("/adm/api/runtime-control/states")    @Operation(operationId="admRuntimeControlFindStateCatalog", summary="Runtime 상태 코드 조회")
    public ResponseEntity<CpfRuntimeStateCatalogResponse> states(HttpServletRequest request){
        operator(request);
        return ResponseEntity.ok(new CpfRuntimeStateCatalogResponse(
                CpfRuntimeStateCatalog.changeStates(),CpfRuntimeStateCatalog.deliveryStates(),
                CpfRuntimeStateCatalog.ackStates(),CpfRuntimeStateCatalog.driftStates()));
    }

    @GetMapping("/adm/api/runtime-control/capabilities")    @Operation(operationId="admRuntimeControlFindCapabilities", summary="Runtime Capability 목록",description="ADM 실시간 운영·제어의 14개 독립 Capability와 승인 필요 여부를 반환합니다.")
    public ResponseEntity<List<CpfRuntimeCapabilityCatalog.Capability>> capabilities(HttpServletRequest request){
        operator(request);
        return ResponseEntity.ok(CpfRuntimeCapabilityCatalog.capabilities());
    }

    @PostMapping("/adm/api/runtime-control/preview-targets")    @Operation(operationId="admRuntimeControlPreviewTargets", summary="Runtime 변경 대상 Preview")
    public ResponseEntity<CpfRuntimeTargetPreview> previewTargets(@RequestBody PreviewTargetRequest body,HttpServletRequest request){
        operator(request);return ResponseEntity.ok(controlPlane.previewTargets(body.changeType(),body.payloadSchemaVersion(),body.target()));
    }

    @PostMapping("/adm/api/runtime-control/preview-change")    @Operation(operationId="admRuntimeControlPreviewChange", summary="Runtime 변경 Preview")
    public ResponseEntity<CpfRuntimeChangePreview> previewChange(@RequestBody RuntimeChangeRequest body,HttpServletRequest request){
        String operator=operator(request);
        CpfRuntimeChangeCommand command=body.toCommand(operator);
        return ResponseEntity.ok(controlPlane.previewChange(command));
    }

    @GetMapping("/adm/api/runtime-control/changes/{changeId}/audit/verify")    @Operation(operationId="admRuntimeControlVerifyAudit", summary="Runtime 변경 Audit Chain 검증")
    public ResponseEntity<CpfRuntimeAuditVerification> verifyAudit(@PathVariable String changeId,HttpServletRequest request){
        operator(request);CpfRuntimeAuditVerification result=controlPlane.verifyAudit(changeId);
        return result.valid()?ResponseEntity.ok(result):ResponseEntity.status(HttpStatus.CONFLICT).body(result);
    }

    @PostMapping("/adm/api/runtime-control/changes/{changeId}/cancel")    @Operation(operationId="admRuntimeControlCancelChange", summary="Runtime 변경 취소")
    public ResponseEntity<CpfRuntimeChangeResult> cancel(@PathVariable String changeId,@RequestBody ControlRequest body,HttpServletRequest request){
        operator(request); throw new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED,"Runtime 변경 취소는 Approval Engine의 RUNTIME_CONTROL_CANCEL Owner Command로 실행해야 합니다.");
    }

    @PostMapping("/adm/api/runtime-control/changes/{changeId}/rollback")    @Operation(operationId="admRuntimeControlRollbackChange", summary="Runtime 변경 Rollback")
    public ResponseEntity<CpfRuntimeChangeResult> rollback(@PathVariable String changeId,@RequestBody ControlRequest body,HttpServletRequest request){
        operator(request); throw new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED,"Runtime Rollback은 Approval Engine의 RUNTIME_CONTROL_ROLLBACK Owner Command로 실행해야 합니다.");
    }

    @PostMapping("/adm/api/runtime-control/groups")    @Operation(operationId="admRuntimeControlSaveGroup", summary="Runtime 대상 그룹 저장")
    public ResponseEntity<CpfRuntimeGroupResult> saveGroup(@RequestBody RuntimeGroupRequest body,HttpServletRequest request){
        String operator=operator(request);
        CpfRuntimeGroupCommand command=body.toCommand(operator);
        CpfRuntimeGroupResult result=controlPlane.saveGroup(command);
        audit(request,operator,"RUNTIME_GROUP_SAVE",result.groupId(),command.reason(),result);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/adm/api/runtime-control/groups/{groupId}")    @Operation(operationId="admRuntimeControlFindGroup", summary="Runtime 대상 그룹 조회")
    public ResponseEntity<CpfRuntimeGroupResult> getGroup(@PathVariable String groupId,HttpServletRequest request){operator(request);return ResponseEntity.ok(controlPlane.getGroup(groupId));}

    @PostMapping("/adm/api/runtime-control/groups/{groupId}/members")    @Operation(operationId="admRuntimeControlChangeGroupMember", summary="Runtime 대상 그룹 구성원 변경")
    public ResponseEntity<CpfRuntimeGroupResult> groupMember(@PathVariable String groupId,@RequestBody RuntimeGroupMemberRequest body,HttpServletRequest request){
        String operator=operator(request);
        if(!groupId.equals(body.groupId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"groupId mismatch");
        CpfRuntimeGroupMemberCommand command=body.toCommand(operator);
        CpfRuntimeGroupResult result=controlPlane.changeGroupMember(command);
        audit(request,operator,"RUNTIME_GROUP_MEMBER",groupId,command.reason(),result);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/adm/api/runtime-control/groups/{groupId}")    @Operation(operationId="admRuntimeControlDeleteGroup", summary="Runtime 대상 그룹 삭제")
    public ResponseEntity<Void> deleteGroup(@PathVariable String groupId,@RequestParam String operationId,@RequestParam long expectedVersion,@RequestParam String reason,HttpServletRequest request){
            String operator=operator(request);controlPlane.deleteGroup(groupId,operationId,expectedVersion,reason,operator);audit(request,operator,"RUNTIME_GROUP_DELETE",groupId,reason,Map
            .of("deleted",true));return ResponseEntity.noContent().build();}

    @PostMapping("/cpf/runtime-control/agent/register") @Operation(operationId="cpfRuntimeAgentRegister", summary="Runtime Agent 자기등록")
    public ResponseEntity<CpfRuntimeInstanceLease> register(@RequestHeader(TOKEN_HEADER)String token,@RequestBody CpfRuntimeInstanceRegistration registration){agent(token);return ResponseEntity
            .ok(controlPlane.register(registration));}

    @PostMapping("/cpf/runtime-control/agent/heartbeat") @Operation(operationId="cpfRuntimeAgentHeartbeat", summary="Runtime Agent lease heartbeat")
    public ResponseEntity<CpfRuntimeInstanceLease> heartbeat(@RequestHeader(TOKEN_HEADER)String token,@RequestBody HeartbeatRequest body){
        agent(token);
        return ResponseEntity.ok(controlPlane.heartbeat(body.instanceId(),body.fencingToken(),body.actualHash(),
                body.actualVersion(),body.agentTime()==null?Instant.now():body.agentTime()));
    }

    @PostMapping("/cpf/runtime-control/agent/deregister") @Operation(operationId="cpfRuntimeAgentDeregister", summary="Runtime Agent graceful deregistration")
    public ResponseEntity<Void> deregister(@RequestHeader(TOKEN_HEADER)String token,@RequestBody DeregisterRequest body){
        agent(token);controlPlane.deregister(body.instanceId(),body.fencingToken(),body.reason());return ResponseEntity.noContent().build();
    }

    @PostMapping("/cpf/runtime-control/agent/actual-state") @Operation(operationId="cpfRuntimeAgentActualState", summary="Runtime Agent durable actual state 재보고")
    public ResponseEntity<Void> actualState(@RequestHeader(TOKEN_HEADER)String token,@RequestBody ActualStateRequest body){
        agent(token);controlPlane.reconcileActualState(body.instanceId(),body.fencingToken(),body.states());return ResponseEntity.noContent().build();
    }

    @PostMapping("/cpf/runtime-control/agent/claim") @Operation(operationId="cpfRuntimeAgentClaim", summary="Runtime Agent durable delivery claim")
    public ResponseEntity<List<CpfRuntimeDelivery>> claim(@RequestHeader(TOKEN_HEADER)String token,@RequestBody ClaimRequest body){agent(token);return ResponseEntity.ok(controlPlane.claim(body
            .instanceId(),body.fencingToken(),body.limit()));}

    @PostMapping("/cpf/runtime-control/agent/ack") @Operation(operationId="cpfRuntimeAgentAck", summary="Runtime Agent ACK")
    public ResponseEntity<CpfRuntimeChangeResult> ack(@RequestHeader(TOKEN_HEADER)String token,@RequestBody CpfRuntimeAck ack){agent(token);return ResponseEntity.ok(controlPlane.acknowledge(ack));}

    @ExceptionHandler(CpfRuntimeRateLimitException.class)
    public ResponseEntity<AdmApiErrorResponse> rateLimit(CpfRuntimeRateLimitException ex){
        return error(HttpStatus.TOO_MANY_REQUESTS,"CPF_RUNTIME_RATE_LIMIT",ex.getMessage());
    }

    @ExceptionHandler({CpfRuntimeVersionConflictException.class,CpfRuntimeFenceException.class})
    public ResponseEntity<AdmApiErrorResponse> conflict(RuntimeException ex){
        return error(HttpStatus.CONFLICT,"CPF_RUNTIME_CONFLICT",ex.getMessage());
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<AdmApiErrorResponse> unavailable(DataAccessException ex){
        return error(HttpStatus.SERVICE_UNAVAILABLE,"CPF_RUNTIME_CONTROL_STORE_UNAVAILABLE",
                "Runtime Control Store를 사용할 수 없습니다.");
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<AdmApiErrorResponse> badRequest(IllegalArgumentException ex){
        return error(HttpStatus.BAD_REQUEST,"CPF_RUNTIME_BAD_REQUEST",ex.getMessage());
    }

    private ResponseEntity<AdmApiErrorResponse> error(HttpStatus status,String code,String message){
        return ResponseEntity.status(status).body(new AdmApiErrorResponse(
                code,message==null?"Runtime Control 요청 처리 실패":message,Instant.now()));
    }

    private void requireRiskApproval(CpfRuntimeChangeCommand c){
        if(blank(c.reason()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Runtime 변경 사유는 필수입니다.");
        if(blank(c.operationId()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"operationId는 필수입니다.");
        if(c.expectedVersion()==null)throw new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED,"expectedVersion 기반 CAS 검증이 필요합니다.");
        if(CpfRuntimeCapabilityCatalog.requiresApproval(c.changeType())){
            throw new ResponseStatusException(HttpStatus.PRECONDITION_REQUIRED,
                    "위험 Runtime 변경은 Approval Engine의 RUNTIME_CONTROL_CREATE Owner Command로 실행해야 합니다. approvalId 문자열은 실행 증명이 아닙니다.");
        }
    }
    private boolean blank(String v){return v==null||v.isBlank();}
    private String operator(HttpServletRequest request){Object value=request.getAttribute("adm.operatorId");if(value instanceof String s&&!s.isBlank())return s;throw new
            ResponseStatusException(HttpStatus.UNAUTHORIZED,"검증된 ADM operator session이 필요합니다.");}
    private void agent(String provided){
        if(agentToken.isBlank())throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,"Runtime Agent credential이 구성되지 않았습니다.");
        byte[] a=agentToken.getBytes(StandardCharsets.UTF_8),b=(provided==null?"":provided).getBytes(StandardCharsets.UTF_8);
        if(!MessageDigest.isEqual(a,b))throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Runtime Agent 인증에 실패했습니다.");
    }
    private void audit(HttpServletRequest req,String user,String action,String id,String reason,Object after){
        audit.record(CpfContexts.transactionId(),user,action,"cpf_runtime_change",id,reason,"",String.valueOf(after),"Runtime Control Plane",req.getRemoteAddr());
    }


    /** Browser가 운영자 ID를 제출하지 못하게 하고, 검증된 Session operator를 서버에서 주입합니다. */
    public record RuntimeChangeRequest(
            String operationId,
            String changeType,
            int payloadSchemaVersion,
            CpfRuntimeTargetSelector target,
            CpfRuntimePayload payload,
            Long expectedVersion,
            String rolloutMode,
            Integer waveSize,
            Integer quorumPercent,
            Instant scheduledAt,
            Instant expiresAt,
            String reason,
            String approvalId,
            String breakGlassId) {
        CpfRuntimeChangeCommand toCommand(String operator) {
            return new CpfRuntimeChangeCommand(operationId, changeType, payloadSchemaVersion, target, payload,
                    expectedVersion, rolloutMode, waveSize, quorumPercent, scheduledAt, expiresAt,
                    reason, approvalId, breakGlassId, operator);
        }
    }

    /** Runtime Group의 requestedBy는 Request Body가 아니라 인증 Session에서만 결정합니다. */
    public record RuntimeGroupRequest(
            String operationId,
            String groupId,
            String groupName,
            String parentGroupId,
            String environment,
            String description,
            Long expectedVersion,
            boolean active,
            String reason) {
        CpfRuntimeGroupCommand toCommand(String operator) {
            return new CpfRuntimeGroupCommand(operationId, groupId, groupName, parentGroupId, environment,
                    description, expectedVersion, active, reason, operator);
        }
    }

    /** Runtime Group Member 변경도 인증 Session의 operator를 서버에서 주입합니다. */
    public record RuntimeGroupMemberRequest(
            String operationId,
            String groupId,
            String instanceId,
            boolean active,
            String reason) {
        CpfRuntimeGroupMemberCommand toCommand(String operator) {
            return new CpfRuntimeGroupMemberCommand(operationId, groupId, instanceId, active, reason, operator);
        }
    }

    public record ControlRequest(String operationId,String reason){}
    public record PreviewTargetRequest(String changeType,int payloadSchemaVersion,CpfRuntimeTargetSelector target){}
    public record HeartbeatRequest(String instanceId,long fencingToken,String actualHash,long actualVersion,Instant agentTime){}
    public record DeregisterRequest(String instanceId,long fencingToken,String reason){}
    public record ActualStateRequest(String instanceId,long fencingToken,List<CpfRuntimeActualState> states){}
    public record ClaimRequest(String instanceId,long fencingToken,int limit){}
}
