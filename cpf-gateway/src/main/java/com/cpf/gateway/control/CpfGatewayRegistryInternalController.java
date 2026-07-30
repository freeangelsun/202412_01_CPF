package com.cpf.gateway.control;

import com.cpf.core.api.gateway.CpfGatewayRegistryPort;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** ADM Remote Port가 호출하는 Gateway Owner 내부 Control API입니다. */
@RestController
@RequestMapping("/internal/v1/gateway/registry")
@ConditionalOnProperty(prefix = "cpf.gateway.control", name = "enabled", havingValue = "true")
public final class CpfGatewayRegistryInternalController {
    private final CpfGatewayRegistryPort registry;

    public CpfGatewayRegistryInternalController(CpfGatewayRegistryPort registry) {
        this.registry = registry;
    }

    @GetMapping("/server-groups")
    public List<CpfGatewayRegistryPort.ServerGroup> groups(
            @RequestParam(required = false) String environmentCode,
            @RequestParam(required = false) String serviceId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "100") int limit) {
        return registry.findServerGroups(environmentCode, serviceId, status, limit);
    }

    @GetMapping("/server-groups/{serverGroupId}/members")
    public List<CpfGatewayRegistryPort.GroupMember> members(@PathVariable String serverGroupId) {
        return registry.findMembers(serverGroupId);
    }

    @GetMapping("/bindings")
    public List<CpfGatewayRegistryPort.GatewayBinding> bindings(
            @RequestParam(required = false) String environmentCode,
            @RequestParam(required = false) String routeId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "100") int limit) {
        return registry.findBindings(environmentCode, routeId, status, limit);
    }

    @GetMapping("/bindings/{bindingId}/apply-status")
    public List<CpfGatewayRegistryPort.ApplyStatus> applyStatus(
            @PathVariable String bindingId, @RequestParam(defaultValue = "100") int limit) {
        return registry.findApplyStatuses(bindingId, limit);
    }

    @GetMapping("/bindings/{bindingId}/connection-tests")
    public List<CpfGatewayRegistryPort.ConnectionTestResult> connectionTests(
            @PathVariable String bindingId, @RequestParam(defaultValue = "100") int limit) {
        return registry.findConnectionTests(bindingId, limit);
    }

    @PostMapping("/server-groups")
    public CpfGatewayRegistryPort.MutationResult saveGroup(
            @RequestBody CpfGatewayRegistryPort.ServerGroupCommand command, HttpServletRequest request) {
        return registry.saveServerGroup(new CpfGatewayRegistryPort.ServerGroupCommand(
                command.operationId(), command.serverGroupId(), command.groupName(), command.environmentCode(),
                command.serviceId(), command.endpointCode(), command.targetProtocol(), command.loadBalancePolicy(),
                command.hashKeySource(), command.healthPolicyId(), command.failoverGroupId(), command.directAllowed(),
                command.members(), command.expectedVersion(), command.reason(), operator(request)));
    }

    @PostMapping("/bindings")
    public CpfGatewayRegistryPort.MutationResult saveBinding(
            @RequestBody CpfGatewayRegistryPort.GatewayBindingCommand command, HttpServletRequest request) {
        return registry.saveBinding(new CpfGatewayRegistryPort.GatewayBindingCommand(
                command.operationId(), command.bindingId(), command.route(), command.serverGroupId(),
                command.gatewayAllowed(), command.directAllowed(), command.approvalId(), command.effectiveFrom(),
                command.effectiveTo(), command.expectedVersion(), command.reason(), operator(request)));
    }

    @PostMapping("/bindings/{bindingId}/state")
    public CpfGatewayRegistryPort.MutationResult changeBindingState(
            @PathVariable String bindingId,
            @RequestBody CpfGatewayRegistryPort.BindingStateCommand command,
            HttpServletRequest request) {
        requireSame(bindingId, command.bindingId(), "bindingId");
        return registry.changeBindingState(new CpfGatewayRegistryPort.BindingStateCommand(
                command.operationId(), command.bindingId(), command.targetState(), command.expectedVersion(),
                command.approvalId(), command.reason(), operator(request)));
    }

    @DeleteMapping("/server-groups/{serverGroupId}")
    public void retireServerGroup(
            @PathVariable String serverGroupId,
            @RequestBody CpfGatewayRegistryPort.DeleteCommand command,
            HttpServletRequest request) {
        registry.deleteServerGroup(serverGroupId, new CpfGatewayRegistryPort.DeleteCommand(
                command.operationId(), command.expectedVersion(), command.reason(), operator(request)));
    }

    @DeleteMapping("/bindings/{bindingId}")
    public void retireBinding(
            @PathVariable String bindingId,
            @RequestBody CpfGatewayRegistryPort.DeleteCommand command,
            HttpServletRequest request) {
        registry.deleteBinding(bindingId, new CpfGatewayRegistryPort.DeleteCommand(
                command.operationId(), command.expectedVersion(), command.reason(), operator(request)));
    }

    @PostMapping("/apply-ack")
    public CpfGatewayRegistryPort.ApplyStatus acknowledge(
            @RequestBody CpfGatewayRegistryPort.ApplyAckCommand command) {
        return registry.acknowledge(command);
    }

    @PostMapping("/connection-test-results")
    public CpfGatewayRegistryPort.ConnectionTestResult recordConnectionTest(
            @RequestBody CpfGatewayRegistryPort.ConnectionTestCommand command,
            HttpServletRequest request) {
        return registry.recordConnectionTest(new CpfGatewayRegistryPort.ConnectionTestCommand(
                command.testId(), command.bindingId(), command.gatewayInstanceId(), command.instanceId(),
                command.testType(), command.status(), command.failureStage(), command.durationMs(), command.traceId(),
                command.operationId(), command.testedAt(), operator(request)));
    }

    @PostMapping("/connection-test-operations")
    public CpfGatewayRegistryPort.ConnectionTestOperation requestConnectionTest(
            @RequestBody CpfGatewayRegistryPort.ConnectionTestRequest command,
            HttpServletRequest request) {
        return registry.requestConnectionTest(new CpfGatewayRegistryPort.ConnectionTestRequest(
                command.operationId(), command.bindingId(), command.testType(), command.reason(),
                command.payloadHash(), command.expiresAt(), operator(request)));
    }

    @GetMapping("/connection-test-operations/{operationId}")
    public CpfGatewayRegistryPort.ConnectionTestOperation connectionTestOperation(@PathVariable String operationId) {
        return registry.findConnectionTestOperation(operationId);
    }

    @PostMapping("/connection-test-operations/{operationId}/cancel")
    public CpfGatewayRegistryPort.ConnectionTestOperation cancelConnectionTest(
            @PathVariable String operationId,
            @RequestBody CpfGatewayRegistryPort.ConnectionTestCancel command,
            HttpServletRequest request) {
        requireSame(operationId, command.operationId(), "operationId");
        return registry.cancelConnectionTest(new CpfGatewayRegistryPort.ConnectionTestCancel(
                command.operationId(), command.expectedVersion(), command.reason(), operator(request)));
    }

    @PostMapping("/connection-test-operations/{operationId}/revalidate")
    public CpfGatewayRegistryPort.ConnectionTestOperation revalidateConnectionTest(
            @PathVariable String operationId,
            @RequestBody CpfGatewayRegistryPort.ConnectionTestRevalidation command,
            HttpServletRequest request) {
        requireSame(operationId, command.sourceOperationId(), "sourceOperationId");
        return registry.revalidateConnectionTest(new CpfGatewayRegistryPort.ConnectionTestRevalidation(
                command.sourceOperationId(), command.newOperationId(), command.payloadHash(), command.expiresAt(),
                command.reason(), operator(request)));
    }

    @PostMapping("/connection-test-operations/claim")
    public List<CpfGatewayRegistryPort.ConnectionTestOperation> claimConnectionTests(
            @RequestBody ClaimConnectionTests command) {
        return registry.claimConnectionTests(command.gatewayInstanceId(), command.limit());
    }

    @PostMapping("/connection-test-operations/{operationId}/complete")
    public CpfGatewayRegistryPort.ConnectionTestOperation completeConnectionTest(
            @PathVariable String operationId,
            @RequestBody CpfGatewayRegistryPort.ConnectionTestCompletion command) {
        requireSame(operationId, command.operationId(), "operationId");
        return registry.completeConnectionTest(command);
    }

    @PostMapping("/health-probes/claim")
    public List<CpfGatewayRegistryPort.HealthProbeTarget> claimHealthProbes(@RequestBody ClaimHealthProbes command) {
        return registry.claimHealthProbes(command.gatewayInstanceId(), command.limit(), command.leaseSeconds());
    }

    @PostMapping("/health-probes/claim-one")
    public CpfGatewayRegistryPort.HealthProbeTarget claimHealthProbe(@RequestBody ClaimHealthProbe command) {
        return registry.claimHealthProbe(command.serverGroupId(), command.instanceId(),
                command.gatewayInstanceId(), command.leaseSeconds());
    }

    @PostMapping("/health-probes/report")
    public void reportHealth(@RequestBody CpfGatewayRegistryPort.HealthProbeResult command) {
        registry.reportHealth(command);
    }

    @GetMapping("/operations/snapshot")
    public CpfGatewayRegistryPort.OperationsSnapshot operationsSnapshot() {
        return registry.operationsSnapshot();
    }

    @GetMapping("/operations/events")
    public List<CpfGatewayRegistryPort.OperationsEvent> operationsEvents(
            @RequestParam(required = false) String afterEventId,
            @RequestParam(defaultValue = "100") int limit) {
        return registry.operationsEvents(afterEventId, limit);
    }

    private static String operator(HttpServletRequest request) {
        Object value = request.getAttribute("gateway.control.operatorId");
        if (value instanceof String operator && !operator.isBlank()) return operator;
        throw new SecurityException("검증된 Gateway Control 운영자가 없습니다.");
    }

    private static void requireSame(String path, String body, String field) {
        if (path == null || !path.equals(body)) {
            throw new IllegalArgumentException("Path와 Body " + field + "가 다릅니다.");
        }
    }

    public record ClaimConnectionTests(String gatewayInstanceId, int limit) {}
    public record ClaimHealthProbes(String gatewayInstanceId, int limit, long leaseSeconds) {}
    public record ClaimHealthProbe(String serverGroupId, String instanceId, String gatewayInstanceId, long leaseSeconds) {}
}
