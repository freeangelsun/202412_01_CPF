package com.cpf.batch.control.deploy;

import com.cpf.batch.api.DeploymentCellManifest;
import com.cpf.batch.spi.RuntimeHealthProbe;
import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 배포 대상 Runtime의 Liveness/Readiness를 제한된 HTTP(S) Endpoint로 확인합니다.
 * Host, Port, Path와 전송 보안 정책을 검증한 뒤에만 요청하며 Redirect는 사용하지 않습니다.
 */
@Component
public final class HttpRuntimeHealthProbe implements RuntimeHealthProbe {
    private final RestClient.Builder builder;
    private final String scheme;
    private final boolean allowInsecureLoopback;

    public HttpRuntimeHealthProbe(
            RestClient.Builder builder,
            @Value("${cpf.batch.deployment.health-scheme:https}") String scheme,
            @Value("${cpf.batch.deployment.allow-insecure-loopback:true}") boolean allowInsecureLoopback) {
        this.builder = Objects.requireNonNull(builder, "builder");
        this.scheme = normalizeScheme(scheme);
        this.allowInsecureLoopback = allowInsecureLoopback;
    }

    @Override
    public Health probe(DeploymentCellManifest.Instance instance, String path, int timeoutSeconds) {
        Objects.requireNonNull(instance, "instance");
        int timeout = Math.max(1, Math.min(timeoutSeconds, 120));
        try {
            URI endpoint = endpoint(instance, path);
            HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(Math.min(timeout, 20)))
                    .followRedirects(HttpClient.Redirect.NEVER)
                    .build();
            JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
            requestFactory.setReadTimeout(Duration.ofSeconds(timeout));
            RestClient client = builder.clone().requestFactory(requestFactory).build();
            Map<?, ?> body = client.get().uri(endpoint).retrieve().body(Map.class);
            String status = String.valueOf(body == null ? null : body.get("status"));
            Object readyValue = body == null ? null : body.get("ready");
            boolean live = "UP".equalsIgnoreCase(status);
            boolean ready = live && Boolean.TRUE.equals(readyValue);
            return new Health(live, ready, ready ? "UP_READY" : "STATUS=" + status + ",ready=" + readyValue);
        } catch (SecurityException rejected) {
            return new Health(false, false, "PROBE_POLICY_REJECTED");
        } catch (RuntimeException failure) {
            return new Health(false, false, "PROBE_" + failure.getClass().getSimpleName());
        }
    }

    URI endpoint(DeploymentCellManifest.Instance instance, String path) {
        String host = requireHost(instance.hostAlias());
        int port = instance.port();
        if (port < 1 || port > 65535) throw new SecurityException("HEALTH_PORT_INVALID");
        String normalizedPath = requirePath(path);
        if ("http".equals(scheme) && !(allowInsecureLoopback && isLoopback(host))) {
            throw new SecurityException("INSECURE_HEALTH_ENDPOINT_REJECTED");
        }
        try {
            URI endpoint = new URI(scheme, null, host, port, normalizedPath, null, null).normalize();
            if (!normalizedPath.equals(endpoint.getRawPath())) throw new SecurityException("HEALTH_PATH_NORMALIZATION_REJECTED");
            return endpoint;
        } catch (SecurityException failure) {
            throw failure;
        } catch (Exception failure) {
            throw new SecurityException("HEALTH_ENDPOINT_INVALID", failure);
        }
    }

    private static String requireHost(String host) {
        if (host == null || host.isBlank() || host.length() > 253) throw new SecurityException("HEALTH_HOST_REQUIRED");
        String value = host.trim();
        if (value.indexOf('/') >= 0 || value.indexOf('\\') >= 0 || value.indexOf('@') >= 0
                || value.indexOf('?') >= 0 || value.indexOf('#') >= 0 || containsControl(value)) {
            throw new SecurityException("HEALTH_HOST_INVALID");
        }
        return value;
    }

    private static String requirePath(String path) {
        if (path == null || path.isBlank() || path.length() > 512) throw new SecurityException("HEALTH_PATH_REQUIRED");
        String value = path.trim();
        if (!value.startsWith("/") || value.startsWith("//") || value.indexOf('\\') >= 0
                || value.indexOf('?') >= 0 || value.indexOf('#') >= 0 || containsControl(value)
                || value.contains("/../") || value.endsWith("/..") || value.contains("/./") || value.endsWith("/.")) {
            throw new SecurityException("HEALTH_PATH_INVALID");
        }
        return value;
    }

    private static String normalizeScheme(String scheme) {
        String value = scheme == null ? "" : scheme.trim().toLowerCase(java.util.Locale.ROOT);
        if (!"https".equals(value) && !"http".equals(value)) throw new IllegalArgumentException("Unsupported health scheme");
        return value;
    }

    private static boolean containsControl(String value) {
        for (int i = 0; i < value.length(); i++) if (Character.isISOControl(value.charAt(i))) return true;
        return false;
    }

    private static boolean isLoopback(String host) {
        return "localhost".equalsIgnoreCase(host) || "127.0.0.1".equals(host) || "::1".equals(host)
                || "[::1]".equals(host);
    }
}
