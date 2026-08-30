package com.cpf.integration.http;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.integration.context.CpfIntegrationContext;
import com.cpf.integration.context.CpfIntegrationContexts;
import com.cpf.integration.context.CpfIntegrationContextRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.URI;
import java.net.UnknownHostException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import javax.net.ssl.SSLHandshakeException;

/**
 * Typed JDK HTTP client with explicit pre-dispatch failure and UNKNOWN-result semantics.
 */
public final class CpfTypedHttpClient {
    private static final Set<String> METHODS = Set.of("GET", "HEAD", "POST", "PUT", "PATCH", "DELETE", "OPTIONS");
    private static final Set<String> MUTATION_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final HttpClient client;
    private final CpfHttpClientProperties properties;
    private final CpfIntegrationContextRunner integrationContexts;

    public CpfTypedHttpClient(HttpClient client, CpfHttpClientProperties properties, CpfExecutionIdGenerator executionIds) {
        this.client = Objects.requireNonNull(client, "client");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.integrationContexts = new CpfIntegrationContextRunner(Objects.requireNonNull(executionIds, "executionIds"));
        properties.validate();
    }

    /** Managed CPF call path. Transaction/operation identity is derived only from the bound Context. */
    public Result execute(String method, URI uri, byte[] body, String contentType, Duration deadline) {
        CpfContext parent = CpfContexts.requireCurrent();
        return execute(method, uri, body, contentType, parent.idempotencyKey(), deadline);
    }

    /** Managed CPF call path with an explicit business idempotency key; transaction identity remains Framework-owned. */
    public Result execute(String method, URI uri, byte[] body, String contentType, String idempotencyKey, Duration deadline) {
        String normalizedMethod = normalizeMethod(method);
        validateUri(uri);
        CpfContext parent = CpfContexts.requireCurrent();
        Instant absoluteDeadline = parent.execution().deadline();
        if (deadline != null) {
            Instant requested = Instant.now().plus(deadline);
            if (absoluteDeadline == null || requested.isBefore(absoluteDeadline)) absoluteDeadline = requested;
        }
        try {
            return integrationContexts.call(
                    uri.getHost(), logicalEndpointId(normalizedMethod, uri), idempotencyKey,
                    Math.max(1, parent.execution().attempt()), absoluteDeadline,
                    () -> execute(normalizedMethod, uri, body, contentType, parent.transactionId(), idempotencyKey, deadline));
        } catch (RuntimeException failure) {
            throw failure;
        } catch (Exception failure) {
            throw new IllegalStateException("Integration HTTP call scope failed", failure);
        }
    }

    Result execute(
            String method,
            URI uri,
            byte[] body,
            String contentType,
            String transactionId,
            String idempotencyKey,
            Duration deadline) {
        String normalizedMethod = normalizeMethod(method);
        validateUri(uri);
        String safeTransactionId = requireHeaderValue(transactionId, "transactionId", 100);
        CpfContext bound = CpfContexts.current();
        if (bound != null && !bound.transactionId().equals(safeTransactionId)) {
            throw new SecurityException("HTTP transactionId does not match bound CPF Context");
        }
        String safeIdempotencyKey = null;
        if (properties.isRequireIdempotencyKeyForMutations() && MUTATION_METHODS.contains(normalizedMethod)) {
            safeIdempotencyKey = requireHeaderValue(idempotencyKey, "idempotencyKey", 128);
        } else if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            safeIdempotencyKey = requireHeaderValue(idempotencyKey, "idempotencyKey", 128);
        }
        byte[] safeBody = body == null ? new byte[0] : body.clone();
        if (safeBody.length > properties.getMaxRequestBytes()) {
            throw new IllegalArgumentException("HTTP request exceeds configured limit");
        }
        Duration timeout = boundedTimeout(deadline, properties.getRequestTimeout());
        String mediaType = normalizeContentType(contentType);
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(timeout)
                .header("Content-Type", mediaType)
                .header("Accept", mediaType);
        if (safeIdempotencyKey != null) {
            builder.header("Idempotency-Key", safeIdempotencyKey);
        }
        HttpRequest request = builder.method(normalizedMethod, publisherFor(normalizedMethod, safeBody)).build();

        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());
            byte[] responseBody;
            try (InputStream stream = response.body()) {
                responseBody = readBounded(stream, properties.getMaxResponseBytes());
            } catch (CpfHttpResponseTooLargeException ex) {
                throw ex;
            } catch (IOException ex) {
                throw unknown("HTTP response body failed after dispatch", ex);
            }
            return new Result(response.statusCode(), immutableHeaders(response.headers().map()), responseBody, false);
        } catch (HttpConnectTimeoutException ex) {
            throw new CpfHttpPreDispatchException("HTTP connection timed out before dispatch", ex);
        } catch (HttpTimeoutException ex) {
            throw unknown("HTTP result is unknown after request timeout", ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw unknown("HTTP call interrupted after dispatch may have begun", ex);
        } catch (IOException ex) {
            if (isDeterministicPreDispatchFailure(ex)) {
                throw new CpfHttpPreDispatchException("HTTP transport failed before dispatch", ex);
            }
            throw unknown("HTTP transport failed after dispatch may have begun", ex);
        }
    }


    private static String logicalEndpointId(String method, URI uri) {
        String path = uri.getPath() == null || uri.getPath().isBlank() ? "/" : uri.getPath();
        String value = method + " " + uri.getHost() + path;
        return value.length() <= 160 ? value : value.substring(0, 160);
    }

    private static CpfUnknownHttpResultException unknown(String message, Throwable cause) {
        CpfIntegrationContext current = CpfIntegrationContexts.current();
        String id = (current == null ? "INT" : current.callExecutionId()) + "-UNKNOWN-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        return new CpfUnknownHttpResultException(message, cause, id);
    }

    static boolean isDeterministicPreDispatchFailure(Throwable failure) {
        for (Throwable current = failure; current != null; current = current.getCause()) {
            if (current instanceof ConnectException
                    || current instanceof UnknownHostException
                    || current instanceof NoRouteToHostException
                    || current instanceof SSLHandshakeException
                    || current instanceof HttpConnectTimeoutException) {
                return true;
            }
        }
        return false;
    }

    private static byte[] readBounded(InputStream stream, int maxBytes) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream(Math.min(maxBytes, 8192));
        byte[] buffer = new byte[8192];
        int total = 0;
        int read;
        while ((read = stream.read(buffer)) != -1) {
            total += read;
            if (total > maxBytes) {
                throw new CpfHttpResponseTooLargeException("HTTP response exceeds configured limit");
            }
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }

    private static HttpRequest.BodyPublisher publisherFor(String method, byte[] body) {
        if (("GET".equals(method) || "HEAD".equals(method)) && body.length == 0) {
            return HttpRequest.BodyPublishers.noBody();
        }
        return HttpRequest.BodyPublishers.ofByteArray(body);
    }

    private static Duration boundedTimeout(Duration deadline, Duration configured) {
        if (deadline == null) {
            return configured;
        }
        if (deadline.isZero() || deadline.isNegative()) {
            throw new IllegalArgumentException("deadline must be positive");
        }
        return deadline.compareTo(configured) < 0 ? deadline : configured;
    }

    private static String normalizeMethod(String method) {
        requireText(method, "method");
        String normalized = method.trim().toUpperCase(Locale.ROOT);
        if (!METHODS.contains(normalized)) {
            throw new IllegalArgumentException("unsupported HTTP method: " + normalized);
        }
        return normalized;
    }

    private void validateUri(URI uri) {
        Objects.requireNonNull(uri, "uri");
        String scheme = uri.getScheme();
        if (scheme == null || !(scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"))) {
            throw new IllegalArgumentException("HTTP URI scheme must be http or https");
        }
        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new IllegalArgumentException("HTTP URI host is required");
        }
        if (uri.getUserInfo() != null) {
            throw new IllegalArgumentException("HTTP URI user-info is not allowed");
        }
        if (uri.getFragment() != null) {
            throw new IllegalArgumentException("HTTP URI fragment is not allowed");
        }
        if (!properties.allowsHost(uri.getHost())) {
            throw new SecurityException("HTTP endpoint host is not allowlisted");
        }
    }

    private static String normalizeContentType(String contentType) {
        if (contentType == null || contentType.isBlank()) return "application/octet-stream";
        return requireHeaderValue(contentType, "contentType", 200);
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
    }

    private static String requireHeaderValue(String value, String field, int maxLength) {
        requireText(value, field);
        String normalized = value.trim();
        if (normalized.length() > maxLength) {
            throw new IllegalArgumentException(field + " is too long");
        }
        for (int index = 0; index < normalized.length(); index++) {
            char character = normalized.charAt(index);
            if (character == '\r' || character == '\n' || Character.isISOControl(character)) {
                throw new IllegalArgumentException(field + " contains control characters");
            }
        }
        return normalized;
    }

    private static Map<String, List<String>> immutableHeaders(Map<String, List<String>> headers) {
        return headers.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                value -> value.getKey(),
                entry -> List.copyOf(entry.getValue())));
    }

    /** Immutable HTTP outcome retaining status, normalized headers, body, and idempotent replay state. */
    public record Result(int status, Map<String, List<String>> headers, byte[] body, boolean replayed) {
        public Result {
            headers = Map.copyOf(headers);
            body = body.clone();
        }
        @Override public byte[] body() { return body.clone(); }
    }

    public static final class CpfUnknownHttpResultException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final String unknownOutcomeId;
        public CpfUnknownHttpResultException(String message, Throwable cause, String unknownOutcomeId) {
            super(message, cause);
            this.unknownOutcomeId = Objects.requireNonNull(unknownOutcomeId, "unknownOutcomeId");
        }
        /** 운영 Reconcile/조회에서 원 호출과 연결할 UNKNOWN 식별자입니다. */
        public String unknownOutcomeId() { return unknownOutcomeId; }
    }

    public static final class CpfHttpPreDispatchException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public CpfHttpPreDispatchException(String message, Throwable cause) { super(message, cause); }
    }

    public static final class CpfHttpResponseTooLargeException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        public CpfHttpResponseTooLargeException(String message) { super(message); }
    }
}
