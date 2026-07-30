package com.cpf.gateway.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.cpf.core.api.gateway.CpfGatewayProtocol;
import com.cpf.core.api.gateway.CpfGatewayRegistryPort;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;

class CpfGatewayProbeExecutorTest {
    @Test
    void application500IsDownEvenWhenTcpConnects() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/health", exchange -> {
            exchange.sendResponseHeaders(500, -1);
            exchange.close();
        });
        server.start();
        try {
            CpfGatewayProbeExecutor.ProbeResult result = new CpfGatewayProbeExecutor().execute(
                    target(server.getAddress().getPort()), "APPLICATION");
            assertFalse(result.success());
            assertEquals("UP", result.tcpStatus());
            assertEquals("DOWN", result.applicationStatus());
            assertEquals("APPLICATION", result.failureStage());
            assertEquals("APPLICATION_HTTP_500", result.resultCode());
        } finally {
            server.stop(0);
        }
    }

    @Test
    void requestedTcpTestDoesNotClaimApplicationUp() throws Exception {
        try (java.net.ServerSocket server = new java.net.ServerSocket(0)) {
            Thread accept = Thread.ofVirtual().start(() -> {
                try (java.net.Socket ignored = server.accept()) { }
                catch (Exception ignored) { }
            });
            CpfGatewayProbeExecutor.ProbeResult result = new CpfGatewayProbeExecutor().execute(
                    target(server.getLocalPort()), "TCP");
            assertTrue(result.success());
            assertEquals("UP", result.tcpStatus());
            assertEquals("NOT_TESTED", result.applicationStatus());
            assertEquals(com.cpf.core.api.gateway.CpfGatewayHealthStatus.UNKNOWN, result.overallStatus());
            accept.join();
        }
    }

    private static CpfGatewayRegistryPort.HealthProbeTarget target(int port) {
        return new CpfGatewayRegistryPort.HealthProbeTarget(
                "group", "instance", "gateway", 1L,
                "127.0.0.1", port, CpfGatewayProtocol.HTTP, "/health", 2_000);
    }
}
