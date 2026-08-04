package com.cpf.starter.tcp;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import javax.net.SocketFactory;

/**
 * Synchronous framed TCP client with a bounded connection pool.
 *
 * <p>A request that was written but whose response cannot be confirmed is recorded as
 * {@link CpfTcpUnknownResult} and fails with {@link UnknownResultException}. Capacity rejection
 * happens before socket acquisition and therefore before any external side effect.</p>
 */
public final class CpfTcpClient implements AutoCloseable {
    private final CpfTcpProperties properties;
    private final CpfTcpFrameCodec codec;
    private final ArrayBlockingQueue<Socket> pool;
    private final CpfTcpUnknownResultStore unknownResults;
    private final CpfTcpTlsContextProvider tlsContextProvider;
    private final Semaphore capacity;

    public CpfTcpClient(
            CpfTcpProperties properties,
            CpfTcpUnknownResultStore unknownResults,
            CpfTcpTlsContextProvider tlsContextProvider) {
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
        this.unknownResults = Objects.requireNonNull(unknownResults, "unknownResults must not be null");
        if (properties.isTls() && tlsContextProvider == null) {
            throw new IllegalArgumentException("tlsContextProvider is required when TLS is enabled");
        }
        this.tlsContextProvider = tlsContextProvider;
        this.codec = new CpfTcpFrameCodec(properties);
        this.pool = new ArrayBlockingQueue<>(properties.getPoolSize());
        this.capacity = new Semaphore(properties.getPoolSize(), true);
    }

    public byte[] request(String correlationId, byte[] payload) {
        String normalizedCorrelationId = requireCorrelationId(correlationId);
        byte[] requestPayload = Objects.requireNonNull(payload, "payload must not be null").clone();
        acquireCapacity();
        Socket socket = null;
        boolean written = false;
        try {
            socket = borrow();
            socket.setSoTimeout(toSocketTimeout(properties.getResponseTimeout().toMillis(), "responseTimeout"));
            codec.write(socket.getOutputStream(), requestPayload);
            written = true;
            byte[] response = codec.read(socket.getInputStream());
            release(socket);
            socket = null;
            return response;
        } catch (SocketTimeoutException | EOFException exception) {
            destroy(socket);
            if (written) {
                recordUnknown(normalizedCorrelationId, requestPayload, exception);
            }
            throw new UnknownResultException(normalizedCorrelationId, exception);
        } catch (IOException exception) {
            destroy(socket);
            if (written) {
                recordUnknown(normalizedCorrelationId, requestPayload, exception);
                throw new UnknownResultException(normalizedCorrelationId, exception);
            }
            throw new IllegalStateException("TCP request failed before write", exception);
        } finally {
            capacity.release();
        }
    }

    private void acquireCapacity() {
        try {
            long timeoutMillis = Math.max(1L, properties.getConnectTimeout().toMillis());
            if (!capacity.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS)) {
                throw new RejectedExecutionException("TCP connection pool is exhausted");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new RejectedExecutionException("Interrupted while waiting for TCP connection capacity", exception);
        }
    }

    private Socket borrow() throws IOException {
        Socket candidate;
        while ((candidate = pool.poll()) != null) {
            if (isReusable(candidate)) {
                return candidate;
            }
            destroy(candidate);
        }
        SocketFactory factory = properties.isTls()
                ? tlsContextProvider.current().getSocketFactory()
                : SocketFactory.getDefault();
        Socket created = factory.createSocket();
        created.connect(
                new InetSocketAddress(properties.getHost(), properties.getPort()),
                toSocketTimeout(properties.getConnectTimeout().toMillis(), "connectTimeout"));
        return created;
    }

    private void release(Socket socket) {
        if (!isReusable(socket) || !pool.offer(socket)) {
            destroy(socket);
        }
    }

    private void recordUnknown(String correlationId, byte[] payload, Exception exception) {
        unknownResults.record(new CpfTcpUnknownResult(
                correlationId,
                Instant.now(),
                payload,
                exception.getClass().getSimpleName()));
    }

    private static boolean isReusable(Socket socket) {
        return socket != null
                && socket.isConnected()
                && !socket.isClosed()
                && !socket.isInputShutdown()
                && !socket.isOutputShutdown();
    }

    private static String requireCorrelationId(String correlationId) {
        if (correlationId == null || correlationId.isBlank()) {
            throw new IllegalArgumentException("correlationId must not be blank");
        }
        return correlationId.trim();
    }

    private static int toSocketTimeout(long timeoutMillis, String name) {
        if (timeoutMillis < 1L || timeoutMillis > Integer.MAX_VALUE) {
            throw new IllegalStateException(name + " must be between 1ms and " + Integer.MAX_VALUE + "ms");
        }
        return (int) timeoutMillis;
    }

    private static void destroy(Socket socket) {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
                // Closing an already failed socket is best effort.
            }
        }
    }

    @Override
    public void close() {
        Socket socket;
        while ((socket = pool.poll()) != null) {
            destroy(socket);
        }
    }

    public static final class UnknownResultException extends RuntimeException {
        private static final long serialVersionUID = 1L;
        private final String correlationId;

        public UnknownResultException(String correlationId, Throwable cause) {
            super("TCP UNKNOWN_RESULT: " + correlationId, cause);
            this.correlationId = correlationId;
        }

        public String correlationId() {
            return correlationId;
        }
    }
}
