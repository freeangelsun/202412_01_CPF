package com.cpf.admin.opr.gateway;

import com.cpf.admin.opr.service.AdmAuditLogService;
import com.cpf.core.api.execution.CpfOnlineTransaction;
import com.cpf.core.api.gateway.CpfGatewayRegistryPort;
import com.cpf.core.api.logging.CpfTransactionContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

/** Gateway Registry/Binding의 단일 ADM 편집 Owner API입니다. */
@RestController
@RequestMapping("/adm/api/gateway-registry")
@ConditionalOnBean(CpfGatewayRegistryPort.class)
@Tag(name="ADM-GatewayRegistry",description="Server Group·Route Binding·Apply ACK·Connection Test 운영 API")
public class AdmGatewayRegistryController extends com.cpf.admin.common.base.AdmBaseController {
    private final CpfGatewayRegistryPort port;
    private final AdmAuditLogService audit;

    public AdmGatewayRegistryController(CpfGatewayRegistryPort port, AdmAuditLogService audit) {
        this.port=port; this.audit=audit;
    }

    @GetMapping("/server-groups") @CpfOnlineTransaction(id="OADMGW0010",name="ADMGatewayServerGroups")
    @Operation(operationId="admGatewayFindServerGroups",summary="Server Group 조회")
    public List<CpfGatewayRegistryPort.ServerGroup> groups(@RequestParam(required=false) String environmentCode,
            @RequestParam(required=false) String serviceId,@RequestParam(required=false) String status,
            @RequestParam(defaultValue="100") int limit) { return port.findServerGroups(environmentCode,serviceId,status,limit); }

    @GetMapping("/server-groups/{id}/members") @CpfOnlineTransaction(id="OADMGW0020",name="ADMGatewayGroupMembers")
    @Operation(operationId="admGatewayFindGroupMembers",summary="Server Group Member 조회")
    public List<CpfGatewayRegistryPort.GroupMember> members(@PathVariable String id) { return port.findMembers(id); }

    @GetMapping("/bindings") @CpfOnlineTransaction(id="OADMGW0030",name="ADMGatewayBindings")
    @Operation(operationId="admGatewayFindBindings",summary="Gateway Binding 조회")
    public List<CpfGatewayRegistryPort.GatewayBinding> bindings(@RequestParam(required=false) String environmentCode,
            @RequestParam(required=false) String routeId,@RequestParam(required=false) String status,
            @RequestParam(defaultValue="100") int limit) { return port.findBindings(environmentCode,routeId,status,limit); }

    @GetMapping("/bindings/{id}/apply-status") @CpfOnlineTransaction(id="OADMGW0040",name="ADMGatewayApplyStatus")
    @Operation(operationId="admGatewayFindApplyStatus",summary="Gateway Instance별 적용 상태 조회")
    public List<CpfGatewayRegistryPort.ApplyStatus> applyStatus(@PathVariable String id,
            @RequestParam(defaultValue="100") int limit) { return port.findApplyStatuses(id,limit); }

    @GetMapping("/bindings/{id}/connection-tests") @CpfOnlineTransaction(id="OADMGW0050",name="ADMGatewayConnectionTests")
    @Operation(operationId="admGatewayFindConnectionTests",summary="연결시험 결과 조회")
    public List<CpfGatewayRegistryPort.ConnectionTestResult> tests(@PathVariable String id,
            @RequestParam(defaultValue="100") int limit) { return port.findConnectionTests(id,limit); }

    @PostMapping("/server-groups") @CpfOnlineTransaction(id="OADMGW0060",name="ADMGatewaySaveServerGroup")
    @Operation(operationId="admGatewaySaveServerGroup",summary="Server Group 생성·수정")
    public CpfGatewayRegistryPort.MutationResult saveGroup(@RequestBody CpfGatewayRegistryPort.ServerGroupCommand c,
            HttpServletRequest request) {
        String operator=operator(request); owner(c.requestedBy(),operator); reason(c.reason());
        var result=port.saveServerGroup(c); record(request,operator,"GATEWAY_SERVER_GROUP_SAVE",c.serverGroupId(),c.reason(),result); return result;
    }

    @PostMapping("/bindings") @CpfOnlineTransaction(id="OADMGW0070",name="ADMGatewaySaveBinding")
    @Operation(operationId="admGatewaySaveBinding",summary="Gateway Binding Draft 생성·수정")
    public CpfGatewayRegistryPort.MutationResult saveBinding(@RequestBody CpfGatewayRegistryPort.GatewayBindingCommand c,
            HttpServletRequest request) {
        String operator=operator(request); owner(c.requestedBy(),operator); reason(c.reason());
        var result=port.saveBinding(c); record(request,operator,"GATEWAY_BINDING_SAVE",c.bindingId(),c.reason(),result); return result;
    }

    @PostMapping("/bindings/{id}/state") @CpfOnlineTransaction(id="OADMGW0080",name="ADMGatewayBindingState")
    @Operation(operationId="admGatewayChangeBindingState",summary="Binding 승인·활성·차단·폐기")
    public CpfGatewayRegistryPort.MutationResult changeState(@PathVariable String id,
            @RequestBody CpfGatewayRegistryPort.BindingStateCommand c,HttpServletRequest request) {
        if(!id.equals(c.bindingId())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Path와 Body bindingId가 다릅니다.");
        String operator=operator(request); owner(c.requestedBy(),operator); reason(c.reason());
        var result=port.changeBindingState(c); record(request,operator,"GATEWAY_BINDING_"+c.targetState(),id,c.reason(),result); return result;
    }

    @DeleteMapping("/server-groups/{id}") @CpfOnlineTransaction(id="OADMGW0090",name="ADMGatewayDeleteServerGroup")
    @Operation(operationId="admGatewayDeleteServerGroup",summary="미사용 Server Group 삭제")
    public ResponseEntity<Void> deleteGroup(@PathVariable String id,@RequestBody CpfGatewayRegistryPort.DeleteCommand c,HttpServletRequest request) {
        String operator=operator(request); owner(c.requestedBy(),operator); reason(c.reason());port.deleteServerGroup(id,c);
        record(request,operator,"GATEWAY_SERVER_GROUP_DELETE",id,c.reason(),"deleted");return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/bindings/{id}") @CpfOnlineTransaction(id="OADMGW0100",name="ADMGatewayDeleteBinding")
    @Operation(operationId="admGatewayDeleteBinding",summary="Draft/Retired Binding 삭제")
    public ResponseEntity<Void> deleteBinding(@PathVariable String id,@RequestBody CpfGatewayRegistryPort.DeleteCommand c,HttpServletRequest request) {
        String operator=operator(request); owner(c.requestedBy(),operator); reason(c.reason());port.deleteBinding(id,c);
        record(request,operator,"GATEWAY_BINDING_DELETE",id,c.reason(),"deleted");return ResponseEntity.noContent().build();
    }

    private String operator(HttpServletRequest r) { Object v=r.getAttribute("adm.operatorId");if(v instanceof String s&&!s.isBlank())return s;throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"검증된 ADM 운영자가 필요합니다."); }
    private static void owner(String requestedBy,String operator) { if(!operator.equals(requestedBy))throw new ResponseStatusException(HttpStatus.FORBIDDEN,"requestedBy 불일치"); }
    private static void reason(String value) { if(value==null||value.trim().length()<5)throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"운영 사유는 5자 이상이어야 합니다."); }
    private void record(HttpServletRequest req,String user,String action,String id,String reason,Object after) { audit.record(CpfTransactionContext.transactionId(),user,action,"cpf_gateway_registry",id,reason,"",String.valueOf(after),"Gateway Registry",req.getRemoteAddr()); }
}
