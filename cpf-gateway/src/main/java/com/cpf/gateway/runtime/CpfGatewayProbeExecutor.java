package com.cpf.gateway.runtime;

import com.cpf.core.api.gateway.CpfGatewayHealthStatus;
import com.cpf.core.api.gateway.CpfGatewayProtocol;
import com.cpf.core.api.gateway.CpfGatewayRegistryPort;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.time.Duration;
import java.util.Locale;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.springframework.stereotype.Component;

/** DNS·TCP·TLS·Application Probe를 단계별로 실제 실행하는 Gateway Runtime Owner입니다. */
@Component
public final class CpfGatewayProbeExecutor {

    public ProbeResult execute(CpfGatewayRegistryPort.HealthProbeTarget target, String requestedType) {
        return execute(target, null, requestedType);
    }

    public ProbeResult execute(
            CpfGatewayRegistryPort.HealthProbeTarget target,
            CpfGatewayRegistryPort.GatewayBinding binding,
            String requestedType) {
        TestType type = TestType.parse(requestedType);
        long started = System.nanoTime();
        String network = "NOT_TESTED";
        String tcp = "NOT_TESTED";
        String tls = target.protocol().tls() ? "NOT_TESTED" : "NOT_APPLICABLE";
        String application = "NOT_TESTED";
        try {
            InetAddress.getAllByName(required(target.host(), "host"));
            network = "UP";
            if (type == TestType.NETWORK) {
                return success(network, tcp, tls, application, "DNS_OK", started);
            }

            try (Socket socket = target.protocol().tls()
                    ? SSLSocketFactory.getDefault().createSocket()
                    : new Socket()) {
                socket.connect(new InetSocketAddress(target.host(), target.port()), target.timeoutMs());
                socket.setSoTimeout(target.timeoutMs());
                tcp = "UP";
                if (type == TestType.TCP) {
                    return success(network, tcp, tls, application, "TCP_OK", started);
                }
                if (type == TestType.TLS || type == TestType.APPLICATION || type == TestType.GATEWAY_E2E) {
                    if (!target.protocol().tls() && type == TestType.TLS) {
                        return failure(network, tcp, "NOT_APPLICABLE", application,
                                "TLS_UNSUPPORTED", "TLS", started);
                    }
                    if (target.protocol().tls()) {
                        ((SSLSocket) socket).startHandshake();
                        tls = "UP";
                    }
                    if (type == TestType.TLS) {
                        return success(network, tcp, tls, application, "TLS_OK", started);
                    }
                }
            }

            ApplicationResult app = type == TestType.GATEWAY_E2E
                    ? gatewayE2eProbe(target, binding)
                    : applicationProbe(target);
            application = app.up() ? "UP" : "DOWN";
            if (!app.up()) {
                return failure(network, tcp, tls, application, app.code(), "APPLICATION", started);
            }
            return success(network, tcp, tls, application, app.code(), started);
        } catch (java.net.UnknownHostException ex) {
            return failure("DOWN", tcp, tls, application, "DNS_FAILED", "NETWORK", started);
        } catch (javax.net.ssl.SSLException ex) {
            return failure(network, tcp, "DOWN", application, "TLS_FAILED", "TLS", started);
        } catch (java.net.SocketTimeoutException ex) {
            String stage = "UP".equals(tcp) ? "APPLICATION" : "TCP";
            return failure(network, tcp, tls, application, stage + "_TIMEOUT", stage, started);
        } catch (Exception ex) {
            String stage = "UP".equals(tcp) ? (target.protocol().tls() && !"UP".equals(tls) ? "TLS" : "APPLICATION") : "TCP";
            return failure(network, tcp, tls, application, stage + "_FAILED", stage, started);
        }
    }

    private static ApplicationResult applicationProbe(CpfGatewayRegistryPort.HealthProbeTarget target) throws Exception {
        CpfGatewayProtocol protocol = target.protocol();
        if (protocol != CpfGatewayProtocol.HTTP && protocol != CpfGatewayProtocol.HTTPS) {
            return new ApplicationResult(false, "APPLICATION_PROBE_UNSUPPORTED_" + protocol.name());
        }
        String healthPath = target.healthPath() == null || target.healthPath().isBlank() ? "/actuator/health" : target.healthPath().trim();
        if (!healthPath.startsWith("/") || healthPath.contains("..") || healthPath.indexOf('\\') >= 0) {
            return new ApplicationResult(false, "INVALID_HEALTH_PATH");
        }
        URI uri = new URI(protocol == CpfGatewayProtocol.HTTPS ? "https" : "http", null,
                target.host(), target.port(), healthPath, null, null);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setConnectTimeout(target.timeoutMs());
        connection.setReadTimeout(target.timeoutMs());
        connection.setRequestMethod("GET");
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("Accept", "application/json, text/plain;q=0.5");
        int status = connection.getResponseCode();
        connection.disconnect();
        return status >= 200 && status < 400
                ? new ApplicationResult(true, "APPLICATION_HTTP_" + status)
                : new ApplicationResult(false, "APPLICATION_HTTP_" + status);
    }


    private static ApplicationResult gatewayE2eProbe(
            CpfGatewayRegistryPort.HealthProbeTarget target,
            CpfGatewayRegistryPort.GatewayBinding binding) throws Exception {
        if (binding == null) return new ApplicationResult(false, "GATEWAY_E2E_BINDING_REQUIRED");
        CpfGatewayProtocol protocol = target.protocol();
        if (protocol != CpfGatewayProtocol.HTTP && protocol != CpfGatewayProtocol.HTTPS) {
            return new ApplicationResult(false, "GATEWAY_E2E_UNSUPPORTED_" + protocol.name());
        }
        String configuredMethod = binding.httpMethod() == null ? "" : binding.httpMethod().trim().toUpperCase(Locale.ROOT);
        if (!("GET".equals(configuredMethod) || "HEAD".equals(configuredMethod))) {
            return new ApplicationResult(false, "GATEWAY_E2E_UNSAFE_METHOD_" + configuredMethod);
        }
        String targetPath = binding.targetPath();
        if (targetPath == null || targetPath.isBlank() || targetPath.contains("*")
                || targetPath.contains("{") || targetPath.contains("}")) {
            return new ApplicationResult(false, "GATEWAY_E2E_CONCRETE_TARGET_REQUIRED");
        }
        if (!targetPath.startsWith("/") || targetPath.contains("..") || targetPath.indexOf('\\') >= 0) {
            return new ApplicationResult(false, "GATEWAY_E2E_INVALID_TARGET_PATH");
        }
        URI uri = new URI(protocol == CpfGatewayProtocol.HTTPS ? "https" : "http", null,
                target.host(), target.port(), targetPath, null, null);
        HttpURLConnection connection = (HttpURLConnection) uri.toURL().openConnection();
        connection.setConnectTimeout(target.timeoutMs());
        connection.setReadTimeout(target.timeoutMs());
        connection.setRequestMethod("HEAD");
        connection.setInstanceFollowRedirects(false);
        connection.setRequestProperty("X-CPF-Gateway-Connection-Test", "true");
        int status = connection.getResponseCode();
        connection.disconnect();
        if (status >= 200 && status < 400) return new ApplicationResult(true, "GATEWAY_E2E_HTTP_" + status);
        if (status == 401 || status == 403) return new ApplicationResult(false, "GATEWAY_E2E_AUTH_REQUIRED_" + status);
        return new ApplicationResult(false, "GATEWAY_E2E_HTTP_" + status);
    }

    private static ProbeResult success(String network, String tcp, String tls, String application, String code, long started) {
        CpfGatewayHealthStatus overall = "UP".equals(application)
                ? CpfGatewayHealthStatus.UP
                : CpfGatewayHealthStatus.UNKNOWN;
        return new ProbeResult(true, network, tcp, tls, application, overall,
                code, "", elapsed(started));
    }

    private static ProbeResult failure(
            String network, String tcp, String tls, String application, String code, String stage, long started) {
        return new ProbeResult(false, network, tcp, tls, application, CpfGatewayHealthStatus.DOWN,
                code, stage, elapsed(started));
    }

    private static long elapsed(long started) {
        return Math.max(0L, Duration.ofNanos(System.nanoTime() - started).toMillis());
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        return value.trim();
    }

    enum TestType {
        NETWORK, TCP, TLS, APPLICATION, GATEWAY_E2E;
        static TestType parse(String value) {
            try {
                return valueOf(value == null ? "APPLICATION" : value.trim().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException("지원하지 않는 Gateway Connection Test Type입니다: " + value, ex);
            }
        }
    }

    public record ProbeResult(
            boolean success,
            String networkStatus,
            String tcpStatus,
            String tlsStatus,
            String applicationStatus,
            CpfGatewayHealthStatus overallStatus,
            String resultCode,
            String failureStage,
            long durationMs) {
    }

    private record ApplicationResult(boolean up, String code) {
    }
}
