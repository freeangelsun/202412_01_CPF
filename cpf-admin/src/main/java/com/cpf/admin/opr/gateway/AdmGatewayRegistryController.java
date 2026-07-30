package com.cpf.admin.opr.gateway;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.gateway.CpfGatewayRegistryPort;
import com.cpf.core.api.gateway.CpfGatewayProtocol;
import com.cpf.core.api.gateway.CpfGatewayLoadBalancePolicy;
import com.cpf.core.api.logging.CpfTransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;

/** Gateway Registry/Binding의 단일 ADM 편집 Owner API입니다. */
@RestController
@RequestMapping("/adm/api/gateway-registry")
@Tag(name="ADM-GatewayRegistry",description="Server Group·Route Binding·Apply ACK·Connection Test 운영 API")
public class AdmGatewayRegistryController extends com.cpf.admin.common.base.AdmBaseController {
    private final ObjectProvider<CpfGatewayRegistryPort> portProvider;
    private final AdmAuditLogService audit;

    public AdmGatewayRegistryController(
            ObjectProvider<CpfGatewayRegistryPort> portProvider,
            AdmAuditLogService audit) {
        this.portProvider=portProvider; this.audit=audit;
    }

    @GetMapping("/capability")
    @Operation(operationId="admGatewayCapability",summary="Gateway Control Plane 설치·연결 상태")
    public Map<String,Object> capability() {
        CpfGatewayRegistryPort resolved=portProvider.getIfAvailable();
        Map<String,Object> catalog=Map.of(
                "protocols",java.util.Arrays.stream(CpfGatewayProtocol.values()).map(Enum::name).toList(),
                "loadBalancePolicies",java.util.Arrays.stream(CpfGatewayLoadBalancePolicy.values()).map(Enum::name).toList(),
                "bindingStates",List.of("DRAFT","VALIDATED","APPROVAL_PENDING","APPROVED","ACTIVE","PARTIAL","BLOCKED","RETIRED"),
                "connectionTestTypes",List.of("NETWORK","TCP","TLS","APPLICATION","GATEWAY_E2E"));
        if(resolved==null) return Map.of("installed",false,"available",false,"status","NOT_INSTALLED",
                "reason","Gateway Control Plane Provider가 구성되지 않았습니다.","catalog",catalog);
        try {
            CpfGatewayRegistryPort.OperationsSnapshot snapshot=resolved.operationsSnapshot();
            return Map.of("installed",true,"available",true,"status",snapshot.status(),"catalog",catalog,
                    "sourceInstanceId",snapshot.sourceInstanceId(),"generatedAt",snapshot.generatedAt());
        } catch (RuntimeException ex) {
            return Map.of("installed",true,"available",false,"status","UNAVAILABLE","catalog",catalog,
                    "reason","Gateway Control Plane 운영 조회에 실패했습니다.");
        }
    }

    @GetMapping("/operations/snapshot")
    @CpfOnlineTransaction(id="OADMGW0001",name="ADMGatewayOperationsSnapshot")
    @Operation(operationId="admGatewayOperationsSnapshot",summary="Gateway 운영 KPI·Drift·Spool 상태")
    public CpfGatewayRegistryPort.OperationsSnapshot operationsSnapshot() { return port().operationsSnapshot(); }

    @GetMapping("/operations/events")
    @CpfOnlineTransaction(id="OADMGW0002",name="ADMGatewayOperationsEvents")
    @Operation(operationId="admGatewayOperationsEvents",summary="Gateway 운영 Event 증분 조회")
    public List<CpfGatewayRegistryPort.OperationsEvent> operationsEvents(
            @RequestParam(required=false) String afterEventId,
            @RequestParam(defaultValue="100") int limit) {
        return port().operationsEvents(afterEventId,limit);
    }

    @GetMapping("/server-groups") @CpfOnlineTransaction(id="OADMGW0010",name="ADMGatewayServerGroups")
    @Operation(operationId="admGatewayFindServerGroups",summary="Server Group 조회")
    public List<CpfGatewayRegistryPort.ServerGroup> groups(@RequestParam(required=false) String environmentCode,
            @RequestParam(required=false) String serviceId,@RequestParam(required=false) String status,
            @RequestParam(defaultValue="100") int limit) { return port().findServerGroups(environmentCode,serviceId,status,limit); }

    @GetMapping("/server-groups/{id}/members") @CpfOnlineTransaction(id="OADMGW0020",name="ADMGatewayGroupMembers")
    @Operation(operationId="admGatewayFindGroupMembers",summary="Server Group Member 조회")
    public List<CpfGatewayRegistryPort.GroupMember> members(@PathVariable String id) { return port().findMembers(id); }

    @GetMapping("/bindings") @CpfOnlineTransaction(id="OADMGW0030",name="ADMGatewayBindings")
    @Operation(operationId="admGatewayFindBindings",summary="Gateway Binding 조회")
    public List<CpfGatewayRegistryPort.GatewayBinding> bindings(@RequestParam(required=false) String environmentCode,
            @RequestParam(required=false) String routeId,@RequestParam(required=false) String status,
            @RequestParam(defaultValue="100") int limit) { return port().findBindings(environmentCode,routeId,status,limit); }

    @GetMapping("/bindings/{id}/apply-status") @CpfOnlineTransaction(id="OADMGW0040",name="ADMGatewayApplyStatus")
    @Operation(operationId="admGatewayFindApplyStatus",summary="Gateway Instance별 적용 상태 조회")
    public List<CpfGatewayRegistryPort.ApplyStatus> applyStatus(@PathVariable String id,
            @RequestParam(defaultValue="100") int limit) { return port().findApplyStatuses(id,limit); }

    @GetMapping("/bindings/{id}/connection-tests") @CpfOnlineTransaction(id="OADMGW0050",name="ADMGatewayConnectionTests")
    @Operation(operationId="admGatewayFindConnectionTests",summary="연결시험 결과 조회")
    public List<CpfGatewayRegistryPort.ConnectionTestResult> tests(@PathVariable String id,
            @RequestParam(defaultValue="100") int limit) { return port().findConnectionTests(id,limit); }

    @PostMapping("/server-groups") @CpfOnlineTransaction(id="OADMGW0060",name="ADMGatewaySaveServerGroup")
    @Operation(operationId="admGatewaySaveServerGroup",summary="Server Group 생성·수정")
    public CpfGatewayRegistryPort.MutationResult saveGroup(@RequestBody CpfGatewayRegistryPort.ServerGroupCommand c,
            HttpServletRequest request) {
        String operator=operator(request); reason(c.reason());
        CpfGatewayRegistryPort.ServerGroupCommand trusted=new CpfGatewayRegistryPort.ServerGroupCommand(
                c.operationId(),c.serverGroupId(),c.groupName(),c.environmentCode(),c.serviceId(),
                c.endpointCode(),c.targetProtocol(),c.loadBalancePolicy(),c.hashKeySource(),
                c.healthPolicyId(),c.failoverGroupId(),c.directAllowed(),c.members(),
                c.expectedVersion(),c.reason(),operator);
        var result=port().saveServerGroup(trusted);
        record(request,operator,"GATEWAY_SERVER_GROUP_SAVE",c.serverGroupId(),c.reason(),result); return result;
    }

    @PostMapping("/bindings") @CpfOnlineTransaction(id="OADMGW0070",name="ADMGatewaySaveBinding")
    @Operation(operationId="admGatewaySaveBinding",summary="Gateway Binding Draft 생성·수정")
    public CpfGatewayRegistryPort.MutationResult saveBinding(@RequestBody CpfGatewayRegistryPort.GatewayBindingCommand c,
            HttpServletRequest request) {
        String operator=operator(request); reason(c.reason());
        CpfGatewayRegistryPort.GatewayBindingCommand trusted=new CpfGatewayRegistryPort.GatewayBindingCommand(
                c.operationId(),c.bindingId(),c.route(),c.serverGroupId(),c.gatewayAllowed(),
                c.directAllowed(),c.approvalId(),c.effectiveFrom(),c.effectiveTo(),
                c.expectedVersion(),c.reason(),operator);
        var result=port().saveBinding(trusted);
        record(request,operator,"GATEWAY_BINDING_SAVE",c.bindingId(),c.reason(),result); return result;
    }

    @PostMapping("/bindings/{id}/state") @CpfOnlineTransaction(id="OADMGW0080",name="ADMGatewayBindingState")
    @Operation(operationId="admGatewayChangeBindingState",summary="Binding 승인·활성·차단·폐기")
    public CpfGatewayRegistryPort.MutationResult changeState(@PathVariable String id,
            @RequestBody CpfGatewayRegistryPort.BindingStateCommand c,HttpServletRequest request) {
        if(!id.equals(c.bindingId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Path와 Body bindingId가 다릅니다.");
        String targetState=c.targetState()==null?"":c.targetState().toUpperCase(java.util.Locale.ROOT);
        if(java.util.Set.of("APPROVED","ACTIVE","BLOCKED","RETIRED").contains(targetState))
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "승인·활성·차단·폐기 전환은 ADM Approval Owner 실행 API를 사용해야 합니다.");
        String operator=operator(request); reason(c.reason());
        CpfGatewayRegistryPort.BindingStateCommand trusted=new CpfGatewayRegistryPort.BindingStateCommand(
                c.operationId(),c.bindingId(),c.targetState(),c.expectedVersion(),
                c.approvalId(),c.reason(),operator);
        var result=port().changeBindingState(trusted);
        record(request,operator,"GATEWAY_BINDING_"+c.targetState(),id,c.reason(),result); return result;
    }

    @DeleteMapping("/server-groups/{id}") @CpfOnlineTransaction(id="OADMGW0090",name="ADMGatewayDeleteServerGroup")
    @Operation(operationId="admGatewayDeleteServerGroup",summary="미사용 Server Group 폐기")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id,@RequestBody CpfGatewayRegistryPort.DeleteCommand c,HttpServletRequest request) {
        reason(c.reason());
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Server Group 폐기는 ADM Approval Owner 실행 API를 사용해야 합니다.");
    }

    @DeleteMapping("/bindings/{id}") @CpfOnlineTransaction(id="OADMGW0100",name="ADMGatewayDeleteBinding")
    @Operation(operationId="admGatewayDeleteBinding",summary="Binding 폐기")
    public ResponseEntity<Void> deleteBinding(@PathVariable String id,@RequestBody CpfGatewayRegistryPort.DeleteCommand c,HttpServletRequest request) {
        reason(c.reason());
        throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Gateway Binding 폐기는 ADM Approval Owner 실행 API를 사용해야 합니다.");
    }


    @PostMapping("/bindings/{id}/connection-tests") @CpfOnlineTransaction(id="OADMGW0110",name="ADMGatewayRequestConnectionTest")
    @Operation(operationId="admGatewayRequestConnectionTest",summary="Gateway 비동기 연결시험 요청")
    public CpfGatewayRegistryPort.ConnectionTestOperation requestTest(
            @PathVariable String id, @RequestBody CpfGatewayRegistryPort.ConnectionTestRequest c,
            HttpServletRequest request) {
        if(!id.equals(c.bindingId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Path와 Body bindingId가 다릅니다.");
        String operator=operator(request); reason(c.reason());
        OffsetDateTime expires=c.expiresAt()==null?OffsetDateTime.now().plusMinutes(10):c.expiresAt();
        String hash=sha256(id+"|"+c.testType()+"|"+c.reason()+"|"+expires);
        CpfGatewayRegistryPort.ConnectionTestOperation result=port().requestConnectionTest(
                new CpfGatewayRegistryPort.ConnectionTestRequest(
                        c.operationId(),id,c.testType(),c.reason(),hash,expires,operator));
        record(request,operator,"GATEWAY_CONNECTION_TEST_REQUEST",id,c.reason(),result);
        return result;
    }

    @GetMapping("/connection-test-operations/{operationId}")
    @CpfOnlineTransaction(id="OADMGW0120",name="ADMGatewayConnectionTestOperation")
    @Operation(operationId="admGatewayFindConnectionTestOperation",summary="연결시험 Operation 상태 조회")
    public CpfGatewayRegistryPort.ConnectionTestOperation connectionTestOperation(@PathVariable String operationId) {
        return port().findConnectionTestOperation(operationId);
    }

    @PostMapping("/connection-test-operations/{operationId}/cancel")
    @CpfOnlineTransaction(id="OADMGW0130",name="ADMGatewayCancelConnectionTest")
    @Operation(operationId="admGatewayCancelConnectionTest",summary="대기·실행 중 연결시험 취소")
    public CpfGatewayRegistryPort.ConnectionTestOperation cancelConnectionTest(
            @PathVariable String operationId,
            @RequestBody CpfGatewayRegistryPort.ConnectionTestCancel command,
            HttpServletRequest request) {
        if(!operationId.equals(command.operationId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Path와 Body operationId가 다릅니다.");
        String operator=operator(request); reason(command.reason());
        var result=port().cancelConnectionTest(new CpfGatewayRegistryPort.ConnectionTestCancel(
                operationId,command.expectedVersion(),command.reason(),operator));
        record(request,operator,"GATEWAY_CONNECTION_TEST_CANCEL",operationId,command.reason(),result);
        return result;
    }

    @PostMapping("/connection-test-operations/{operationId}/revalidate")
    @CpfOnlineTransaction(id="OADMGW0140",name="ADMGatewayRevalidateConnectionTest")
    @Operation(operationId="admGatewayRevalidateConnectionTest",summary="완료·만료 연결시험 재검증 요청")
    public CpfGatewayRegistryPort.ConnectionTestOperation revalidateConnectionTest(
            @PathVariable String operationId,
            @RequestBody CpfGatewayRegistryPort.ConnectionTestRevalidation command,
            HttpServletRequest request) {
        if(!operationId.equals(command.sourceOperationId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Path와 Body sourceOperationId가 다릅니다.");
        String operator=operator(request); reason(command.reason());
        OffsetDateTime expires=command.expiresAt()==null?OffsetDateTime.now().plusMinutes(10):command.expiresAt();
        String trustedHash=sha256(operationId+"|"+command.newOperationId()+"|"+expires+"|"+command.reason());
        var result=port().revalidateConnectionTest(new CpfGatewayRegistryPort.ConnectionTestRevalidation(
                operationId,command.newOperationId(),trustedHash,expires,command.reason(),operator));
        record(request,operator,"GATEWAY_CONNECTION_TEST_REVALIDATE",operationId,command.reason(),result);
        return result;
    }

    private CpfGatewayRegistryPort port() {
        CpfGatewayRegistryPort resolved=portProvider.getIfAvailable();
        if(resolved==null) throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Gateway Control Plane이 설치되지 않았거나 연결할 수 없습니다.");
        return resolved;
    }
    private String operator(HttpServletRequest r) { Object v=r.getAttribute("adm.operatorId");if(v instanceof String s&&!s.isBlank())return s;throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"검증된 ADM 운영자가 필요합니다."); }
    private static void reason(String value) { if(value==null||value.trim().length()<5)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"운영 사유는 5자 이상이어야 합니다."); }

    private static String sha256(String value) {
        try {
            byte[] digest=MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 unavailable",ex);
        }
    }

    private void record(HttpServletRequest req,String user,String action,String id,String reason,Object after) { audit.record(CpfTransactionContext.transactionId(),user,action,"cpf_gateway_registry",id,reason,"",String.valueOf(after),"Gateway Registry",req.getRemoteAddr()); }
}
