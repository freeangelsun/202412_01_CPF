package com.cpf.integration.tcp.internal;

import com.cpf.integration.resilience.api.CpfResilienceCallContext;
import com.cpf.integration.resilience.api.CpfResilienceExecutor;
import com.cpf.integration.resilience.api.CpfResilienceOutcome;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Resilience-aware TCP exchange boundary.
 *
 * <p>Failures before the first write attempt are deterministic {@link CpfTcpPreDispatchException}
 * failures. Once a write attempt starts, a response loss or transport error is an
 * {@link CpfTcpUnknownResultException}; callers must reconcile instead of blindly retrying a
 * side-effecting operation.</p>
 */
public final class CpfResilientTcpClient {
    private static final int MAX_SUPPORTED_RESPONSE_BYTES = 16 * 1024 * 1024;

    private final CpfResilienceExecutor resilience;
    private final Clock clock;
    private final SocketFactory socketFactory;

    public CpfResilientTcpClient(CpfResilienceExecutor resilience) {
        this(resilience, Clock.systemUTC(), Socket::new);
    }

    CpfResilientTcpClient(
            CpfResilienceExecutor resilience, Clock clock, SocketFactory socketFactory) {
        this.resilience = Objects.requireNonNull(resilience, "resilience");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.socketFactory = Objects.requireNonNull(socketFactory, "socketFactory");
    }

    /**
     * Compatibility entry point. UNKNOWN operations are fail-closed for timeout retry.
     */
    public CpfResilienceOutcome<byte[]> exchange(
            String operationId,
            String transactionId,
            String idempotencyKey,
            String host,
            int port,
            byte[] request,
            int maxResponseBytes,
            int connectTimeoutMillis,
            int readTimeoutMillis) {
        return exchange(
                operationId,
                transactionId,
                idempotencyKey,
                host,
                port,
                request,
                maxResponseBytes,
                connectTimeoutMillis,
                readTimeoutMillis,
                CpfResilienceCallContext.OperationKind.UNKNOWN,
                false);
    }

    public CpfResilienceOutcome<byte[]> exchangeRead(
            String operationId,
            String transactionId,
            String host,
            int port,
            byte[] request,
            int maxResponseBytes,
            int connectTimeoutMillis,
            int readTimeoutMillis) {
        return exchange(
                operationId,
                transactionId,
                null,
                host,
                port,
                request,
                maxResponseBytes,
                connectTimeoutMillis,
                readTimeoutMillis,
                CpfResilienceCallContext.OperationKind.READ,
                true);
    }

    public CpfResilienceOutcome<byte[]> exchangeWrite(
            String operationId,
            String transactionId,
            String idempotencyKey,
            String host,
            int port,
            byte[] request,
            int maxResponseBytes,
            int connectTimeoutMillis,
            int readTimeoutMillis,
            boolean timeoutRetryAllowed) {
        if (timeoutRetryAllowed && (idempotencyKey == null || idempotencyKey.isBlank())) {
            throw new IllegalArgumentException(
                    "retryable TCP WRITE requires a non-blank idempotencyKey");
        }
        return exchange(
                operationId,
                transactionId,
                idempotencyKey,
                host,
                port,
                request,
                maxResponseBytes,
                connectTimeoutMillis,
                readTimeoutMillis,
                CpfResilienceCallContext.OperationKind.WRITE,
                timeoutRetryAllowed);
    }

    private CpfResilienceOutcome<byte[]> exchange(
            String operationId,
            String transactionId,
            String idempotencyKey,
            String host,
            int port,
            byte[] request,
            int maxResponseBytes,
            int connectTimeoutMillis,
            int readTimeoutMillis,
            CpfResilienceCallContext.OperationKind operationKind,
            boolean timeoutRetryAllowed) {
        String normalizedHost = required(host, "host");
        if (port < 1 || port > 65_535) throw new IllegalArgumentException("port must be 1..65535");
        if (maxResponseBytes < 1 || maxResponseBytes > MAX_SUPPORTED_RESPONSE_BYTES) {
            throw new IllegalArgumentException("invalid maxResponseBytes");
        }
        if (connectTimeoutMillis < 1) {
            throw new IllegalArgumentException("connectTimeoutMillis must be positive");
        }
        if (readTimeoutMillis < 1) {
            throw new IllegalArgumentException("readTimeoutMillis must be positive");
        }
        byte[] requestSnapshot = Objects.requireNonNull(request, "request").clone();

        Map<String, String> attributes = new LinkedHashMap<>();
        attributes.put("transport", "TCP");
        attributes.put("host", normalizedHost);
        attributes.put("port", Integer.toString(port));
        attributes.put(CpfResilienceCallContext.OPERATION_KIND_ATTRIBUTE, operationKind.name());
        attributes.put(
                CpfResilienceCallContext.TIMEOUT_RETRY_ATTRIBUTE,
                Boolean.toString(timeoutRetryAllowed));
        attributes.put(CpfResilienceCallContext.TRACE_SPAN_KIND_ATTRIBUTE, "CLIENT");
        attributes.put(CpfResilienceCallContext.TRACE_SEGMENT_ATTRIBUTE, "tcp.exchange");
        attributes.put("cpf.integration.post-dispatch-failure", "UNKNOWN");

        CpfResilienceCallContext context = CpfResilienceCallContext.now(
                operationId, transactionId, idempotencyKey, attributes, clock);
        return resilience.execute(
                context,
                () -> executeTransport(
                        normalizedHost,
                        port,
                        requestSnapshot,
                        maxResponseBytes,
                        connectTimeoutMillis,
                        readTimeoutMillis));
    }

    private byte[] executeTransport(
            String host,
            int port,
            byte[] request,
            int maxResponseBytes,
            int connectTimeoutMillis,
            int readTimeoutMillis) {
        boolean dispatchStarted = false;
        try (Socket socket = socketFactory.create()) {
            socket.connect(new InetSocketAddress(host, port), connectTimeoutMillis);
            socket.setSoTimeout(readTimeoutMillis);
            OutputStream output = socket.getOutputStream();
            dispatchStarted = true;
            output.write(request);
            output.flush();

            InputStream input = socket.getInputStream();
            ByteArrayOutputStream response = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                if (response.size() + read > maxResponseBytes) {
                    throw new CpfTcpUnknownResultException(
                            "TCP response exceeded the configured limit after request dispatch", null);
                }
                response.write(buffer, 0, read);
                if (read == 0) break;
            }
            return response.toByteArray();
        } catch (CpfTcpUnknownResultException exception) {
            throw exception;
        } catch (IOException exception) {
            if (dispatchStarted) {
                throw new CpfTcpUnknownResultException(
                        "TCP outcome is unknown after request dispatch", exception);
            }
            throw new CpfTcpPreDispatchException("TCP failed before request dispatch", exception);
        }
    }

    private static String required(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " is required");
        return value.trim();
    }

    @FunctionalInterface
    interface SocketFactory {
        Socket create() throws IOException;
    }

    public static final class CpfTcpPreDispatchException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public CpfTcpPreDispatchException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static final class CpfTcpUnknownResultException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        public CpfTcpUnknownResultException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
