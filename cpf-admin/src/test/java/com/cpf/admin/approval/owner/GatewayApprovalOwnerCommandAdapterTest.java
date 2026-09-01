package com.cpf.admin.approval.owner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.cpf.admin.approval.api.AdmApprovalExecutionStatus;
import com.cpf.admin.approval.api.AdmApprovedOperationCommand;
import com.cpf.gateway.api.CpfGatewayProtocol;
import com.cpf.gateway.api.CpfGatewayRegistryPort;
import java.lang.reflect.Proxy;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class GatewayApprovalOwnerCommandAdapterTest {
    @Test
    void supportsOnlyExactOwnerCommandActionTargetTuple() {
        CpfGatewayRegistryPort registry = (CpfGatewayRegistryPort) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{CpfGatewayRegistryPort.class},
                (proxy, method, args) -> { throw new UnsupportedOperationException(method.getName()); });
        GatewayApprovalOwnerCommandAdapter adapter = new GatewayApprovalOwnerCommandAdapter(provider(registry));

        assertEquals(true, adapter.supports("cpf-gateway", "GATEWAY_BINDING_BLOCK", "GATEWAY_BINDING_BLOCK", "GATEWAY_BINDING"));
        assertEquals(false, adapter.supports("cpf-gateway-shadow", "GATEWAY_BINDING_BLOCK", "GATEWAY_BINDING_BLOCK", "GATEWAY_BINDING"));
        assertEquals(false, adapter.supports("cpf-gateway", "GATEWAY_BINDING_BLOCK_EXTRA", "GATEWAY_BINDING_BLOCK", "GATEWAY_BINDING"));
        assertEquals(false, adapter.supports("cpf-gateway", "GATEWAY_BINDING_BLOCK", "GATEWAY_BINDING_BLOCK_EXTRA", "GATEWAY_BINDING"));
        assertEquals(false, adapter.supports("cpf-gateway", "GATEWAY_BINDING_BLOCK", "GATEWAY_BINDING_BLOCK", "GATEWAY_BINDING_SHADOW"));
    }
    @Test
    void approvedBlockExecutesOwnerStateCommand() {
        String hash = "a".repeat(64);
        AtomicReference<CpfGatewayRegistryPort.BindingStateCommand> invoked = new AtomicReference<>();
        CpfGatewayRegistryPort.GatewayBinding binding = new CpfGatewayRegistryPort.GatewayBinding(
                "binding-1", "route-1", "PRD", "*", "/orders/**", "POST", "v1",
                CpfGatewayProtocol.HTTP, CpfGatewayProtocol.HTTP,
                "orders", "group-1", "r1", "tls", "authn", "authz", "headers", "rate",
                "health", 1_000, 3_000, 5_000, 0, false, "", "ACTIVE", true, false,
                "101", null, null, hash, 7L, OffsetDateTime.now());
        CpfGatewayRegistryPort registry = (CpfGatewayRegistryPort) Proxy.newProxyInstance(
                getClass().getClassLoader(), new Class<?>[]{CpfGatewayRegistryPort.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findBindings" -> List.of(binding);
                    case "changeBindingState" -> {
                        var command = (CpfGatewayRegistryPort.BindingStateCommand) args[0];
                        invoked.set(command);
                        yield new CpfGatewayRegistryPort.MutationResult(
                                "GATEWAY_BINDING", command.bindingId(), command.targetState(), 8L,
                                OffsetDateTime.now());
                    }
                    default -> throw new UnsupportedOperationException(method.getName());
                });
        GatewayApprovalOwnerCommandAdapter adapter = new GatewayApprovalOwnerCommandAdapter(provider(registry));
        var result = adapter.execute(new AdmApprovedOperationCommand(
                101L, "approval-command-101", "GATEWAY_BINDING_BLOCK", "cpf-gateway",
                "GATEWAY_BINDING_BLOCK", "GATEWAY_BINDING", "binding-1", hash,
                "requester", "approver", "QA31 approved block", "tx-1"));

        assertEquals(AdmApprovalExecutionStatus.SUCCEEDED, result.status());
        assertEquals("BLOCKED", invoked.get().targetState());
        assertEquals("101", invoked.get().approvalId());
        assertEquals("approver", invoked.get().requestedBy());
    }

    /** Gateway Control 은 opt-in 이므로 Adapter 는 ObjectProvider 로 받는다. 테스트도 같은 계약을 쓴다. */
    private static org.springframework.beans.factory.ObjectProvider<CpfGatewayRegistryPort> provider(CpfGatewayRegistryPort value){
        return new org.springframework.beans.factory.ObjectProvider<>(){
            @Override public CpfGatewayRegistryPort getObject(Object... args){return value;}
            @Override public CpfGatewayRegistryPort getObject(){return value;}
            @Override public CpfGatewayRegistryPort getIfAvailable(){return value;}
            @Override public CpfGatewayRegistryPort getIfUnique(){return value;}
        };
    }
}
