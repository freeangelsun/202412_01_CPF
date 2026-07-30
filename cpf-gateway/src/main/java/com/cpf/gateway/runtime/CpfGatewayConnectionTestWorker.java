package com.cpf.gateway.runtime;

import com.cpf.core.api.gateway.CpfGatewayRegistryPort;
import com.cpf.core.api.runtime.CpfInstanceIdentity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLSocketFactory;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** 비동기 Gateway Connection Test Operation을 단계별 결과와 함께 실행합니다. */
@Component
public final class CpfGatewayConnectionTestWorker {
    private static final Logger log = LoggerFactory.getLogger(CpfGatewayConnectionTestWorker.class);
    private final CpfGatewayRegistryPort registry;

    public CpfGatewayConnectionTestWorker(CpfGatewayRegistryPort registry) {
        this.registry = registry;
    }

    @Scheduled(fixedDelayString = "${cpf.gateway.connection-test.worker-millis:2000}")
    public void run() {
        String gatewayInstanceId = CpfInstanceIdentity.current().serverInstanceId();
        for (CpfGatewayRegistryPort.ConnectionTestOperation operation
                : registry.claimConnectionTests(gatewayInstanceId, 20)) {
            execute(operation, gatewayInstanceId);
        }
    }

    private void execute(CpfGatewayRegistryPort.ConnectionTestOperation operation, String gatewayInstanceId) {
        try {
            CpfGatewayRegistryPort.ConnectionTestOperation current = registry.findConnectionTestOperation(operation.operationId());
            if (completeCancellationOrExpiry(current)) return;
            String bindingId = current.bindingId();
            CpfGatewayRegistryPort.GatewayBinding binding = registry.findBindings(null, null, null, 10_000).stream()
                    .filter(item -> item.bindingId().equals(bindingId))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Binding not found"));
            List<CpfGatewayRegistryPort.GroupMember> members = registry.findMembers(binding.serverGroupId()).stream()
                    .filter(CpfGatewayRegistryPort.GroupMember::enabled).toList();
            if (members.isEmpty()) throw new IllegalStateException("Enabled Gateway member is empty");
            int success = 0;
            for (CpfGatewayRegistryPort.GroupMember member : members) {
                current = registry.findConnectionTestOperation(current.operationId());
                if (completeCancellationOrExpiry(current)) return;
                Probe probe = probe(binding, member);
                if (probe.success()) success++;
                registry.recordConnectionTest(new CpfGatewayRegistryPort.ConnectionTestCommand(
                        UUID.randomUUID().toString(), binding.bindingId(), gatewayInstanceId, member.instanceId(),
                        current.testType(), probe.success() ? "SUCCESS" : "FAILED", probe.failureStage(),
                        probe.durationMs(), UUID.randomUUID().toString(), current.operationId(),
                        OffsetDateTime.now(ZoneOffset.UTC), current.requestedBy()));
            }
            current = registry.findConnectionTestOperation(current.operationId());
            if (completeCancellationOrExpiry(current)) return;
            String state = success == members.size() ? "SUCCESS" : success == 0 ? "FAILED" : "PARTIAL";
            registry.completeConnectionTest(new CpfGatewayRegistryPort.ConnectionTestCompletion(
                    current.operationId(), state, success + "/" + members.size() + " members succeeded",
                    current.version()));
        } catch (RuntimeException ex) {
            try {
                CpfGatewayRegistryPort.ConnectionTestOperation current =
                        registry.findConnectionTestOperation(operation.operationId());
                if (completeCancellationOrExpiry(current)) return;
                if ("RUNNING".equals(current.status())) {
                    registry.completeConnectionTest(new CpfGatewayRegistryPort.ConnectionTestCompletion(
                            current.operationId(), "FAILED", sanitize(ex.getMessage()), current.version()));
                }
            } catch (RuntimeException completionFailure) {
                log.error("Gateway Connection Test 실패 상태 기록에 실패했습니다. operationId={}",
                        operation.operationId(), completionFailure);
            }
        }
    }

    private boolean completeCancellationOrExpiry(CpfGatewayRegistryPort.ConnectionTestOperation current) {
        if (!"RUNNING".equals(current.status())) return true;
        if (current.cancelRequested()) {
            registry.completeConnectionTest(new CpfGatewayRegistryPort.ConnectionTestCompletion(
                    current.operationId(), "CANCELLED", "Cancellation requested", current.version()));
            return true;
        }
        if (current.expiresAt() != null && !current.expiresAt().isAfter(OffsetDateTime.now(ZoneOffset.UTC))) {
            registry.completeConnectionTest(new CpfGatewayRegistryPort.ConnectionTestCompletion(
                    current.operationId(), "STALE", "Connection Test operation expired", current.version()));
            return true;
        }
        return false;
    }

    private Probe probe(CpfGatewayRegistryPort.GatewayBinding binding, CpfGatewayRegistryPort.GroupMember member) {
        long started = System.nanoTime();
        // Member의 실제 Host/Port는 Health Worker가 Service Registry와 결합해 검증합니다.
        // Connection Test는 같은 Member를 즉시 Health Claim하여 최신 결과를 생성하도록 요구합니다.
        CpfGatewayRegistryPort.HealthProbeTarget target = registry.claimHealthProbe(
                binding.serverGroupId(), member.instanceId(),
                CpfInstanceIdentity.current().serverInstanceId(), 30);
        if (target == null) return new Probe(false, "LEASE", elapsed(started));
        try (Socket socket = target.protocol().tls()
                ? SSLSocketFactory.getDefault().createSocket()
                : new Socket()) {
            socket.connect(new InetSocketAddress(target.host(), target.port()), target.timeoutMs());
            if (target.protocol().tls()) ((javax.net.ssl.SSLSocket) socket).startHandshake();
            registry.reportHealth(new CpfGatewayRegistryPort.HealthProbeResult(
                    UUID.randomUUID().toString(), target.serverGroupId(), target.instanceId(), target.gatewayInstanceId(),
                    target.fencingToken(), "UP", "UP", target.protocol().tls() ? "UP" : "NOT_APPLICABLE",
                    "UP", com.cpf.core.api.gateway.CpfGatewayHealthStatus.UP, "CONNECTION_TEST_OK",
                    elapsed(started), OffsetDateTime.now(ZoneOffset.UTC)));
            return new Probe(true, null, elapsed(started));
        } catch (Exception ex) {
            registry.reportHealth(new CpfGatewayRegistryPort.HealthProbeResult(
                    UUID.randomUUID().toString(), target.serverGroupId(), target.instanceId(), target.gatewayInstanceId(),
                    target.fencingToken(), "UP", "DOWN", target.protocol().tls() ? "DOWN" : "NOT_APPLICABLE",
                    "UNKNOWN", com.cpf.core.api.gateway.CpfGatewayHealthStatus.DOWN, "CONNECTION_TEST_FAILED",
                    elapsed(started), OffsetDateTime.now(ZoneOffset.UTC)));
            return new Probe(false, "TCP_OR_TLS", elapsed(started));
        }
    }

    private static long elapsed(long started) { return Math.max(0L, (System.nanoTime()-started)/1_000_000L); }
    private static String sanitize(String value) {
        String text=Objects.toString(value,"Connection test failed").replaceAll("(?i)(password|token|secret)=[^,\\s]+","$1=***");
        return text.length()>1_000?text.substring(0,1_000):text;
    }
    private record Probe(boolean success,String failureStage,long durationMs) {}
}
