package com.cpf.gateway.runtime;

import com.cpf.core.api.gateway.*;
import com.cpf.core.api.runtime.CpfInstanceIdentity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLSocketFactory;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/** Lease/Fencing을 획득한 Gateway 인스턴스만 Protocol Health Probe를 수행합니다. */
@Component
public final class CpfGatewayHealthWorker {
    private final CpfGatewayRegistryPort registry;

    public CpfGatewayHealthWorker(CpfGatewayRegistryPort registry) {
        this.registry = registry;
    }

    @Scheduled(fixedDelayString = "${cpf.gateway.health.worker-millis:10000}")
    public void run() {
        String gatewayInstanceId = CpfInstanceIdentity.current().serverInstanceId();
        for (CpfGatewayRegistryPort.HealthProbeTarget target
                : registry.claimHealthProbes(gatewayInstanceId, 200, 30)) {
            registry.reportHealth(probe(target));
        }
    }

    private CpfGatewayRegistryPort.HealthProbeResult probe(CpfGatewayRegistryPort.HealthProbeTarget target) {
        long started = System.nanoTime();
        String network = "UP";
        String tcp = "DOWN";
        String tls = target.protocol().tls() ? "DOWN" : "NOT_APPLICABLE";
        String application = "UNKNOWN";
        String code = "PROBE_OK";
        CpfGatewayHealthStatus overall = CpfGatewayHealthStatus.UP;
        try (Socket socket = target.protocol().tls()
                ? SSLSocketFactory.getDefault().createSocket()
                : new Socket()) {
            socket.connect(new InetSocketAddress(target.host(), target.port()), target.timeoutMs());
            tcp = "UP";
            if (target.protocol().tls()) {
                ((javax.net.ssl.SSLSocket) socket).startHandshake();
                tls = "UP";
            }
            application = "UP";
        } catch (java.net.UnknownHostException ex) {
            network = "DOWN"; code = "DNS_FAILED"; overall = CpfGatewayHealthStatus.DOWN;
        } catch (java.net.SocketTimeoutException ex) {
            code = "CONNECT_TIMEOUT"; overall = CpfGatewayHealthStatus.DOWN;
        } catch (Exception ex) {
            code = "CONNECT_FAILED"; overall = CpfGatewayHealthStatus.DOWN;
        }
        long duration = Math.max(0L, (System.nanoTime() - started) / 1_000_000L);
        return new CpfGatewayRegistryPort.HealthProbeResult(
                UUID.randomUUID().toString(), target.serverGroupId(), target.instanceId(),
                target.gatewayInstanceId(), target.fencingToken(), network, tcp, tls, application,
                overall, code, duration, OffsetDateTime.now(ZoneOffset.UTC));
    }
}
