package com.cpf.integration.tcp;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class CpfTcpClientTest {
    @Test
    void rejectsBlankCorrelationBeforeNetworkSideEffect() throws Exception {
        try (ServerSocket server = new ServerSocket(0)) {
            server.setSoTimeout(250);
            CpfTcpProperties properties = properties(server.getLocalPort(), 1);
            CpfTcpUnknownResultStore unknown = new CpfTcpUnknownResultStore(10);
            try (CpfTcpClient client = new CpfTcpClient(properties, unknown, null)) {
                IllegalArgumentException failure = assertThrows(
                        IllegalArgumentException.class,
                        () -> client.request("  ", new byte[] {1}));
                assertTrue(failure.getMessage().contains("correlationId"));
                assertThrows(SocketTimeoutException.class, server::accept);
                assertTrue(unknown.snapshot().isEmpty());
            }
        }
    }

    @Test
    void enforcesPoolCapacityBeforeOpeningAnotherSocket() throws Exception {
        try (ServerSocket server = new ServerSocket(0);
                var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            CountDownLatch firstRequestRead = new CountDownLatch(1);
            CountDownLatch releaseResponse = new CountDownLatch(1);
            Future<?> serverFuture = executor.submit(() -> {
                try (Socket socket = server.accept()) {
                    assertArrayEquals(new byte[] {1, 2, 3}, readFrame(socket));
                    firstRequestRead.countDown();
                    assertTrue(releaseResponse.await(2, TimeUnit.SECONDS));
                    writeFrame(socket, new byte[] {9});
                }
                return null;
            });

            CpfTcpProperties properties = properties(server.getLocalPort(), 1);
            properties.setConnectTimeout(Duration.ofMillis(150));
            properties.setResponseTimeout(Duration.ofSeconds(2));
            CpfTcpUnknownResultStore unknown = new CpfTcpUnknownResultStore(10);
            try (CpfTcpClient client = new CpfTcpClient(properties, unknown, null)) {
                Future<byte[]> first = executor.submit(() -> client.request("first", new byte[] {1, 2, 3}));
                assertTrue(firstRequestRead.await(1, TimeUnit.SECONDS));
                RejectedExecutionException failure = assertThrows(
                        RejectedExecutionException.class,
                        () -> client.request("second", new byte[] {4}));
                assertTrue(failure.getMessage().contains("exhausted"));
                releaseResponse.countDown();
                assertArrayEquals(new byte[] {9}, first.get(2, TimeUnit.SECONDS));
                serverFuture.get(2, TimeUnit.SECONDS);
                assertTrue(unknown.snapshot().isEmpty());
            }
        }
    }

    @Test
    void recordsUnknownWhenPeerClosesAfterRequestWrite() throws Exception {
        try (ServerSocket server = new ServerSocket(0);
                var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<?> serverFuture = executor.submit(() -> {
                try (Socket socket = server.accept()) {
                    assertArrayEquals(new byte[] {7, 8}, readFrame(socket));
                }
                return null;
            });
            CpfTcpUnknownResultStore unknown = new CpfTcpUnknownResultStore(10);
            try (CpfTcpClient client = new CpfTcpClient(properties(server.getLocalPort(), 1), unknown, null)) {
                CpfTcpClient.UnknownResultException failure = assertThrows(
                        CpfTcpClient.UnknownResultException.class,
                        () -> client.request("tx-unknown", new byte[] {7, 8}));
                assertTrue(failure.getMessage().contains("tx-unknown"));
                assertTrue(unknown.find("tx-unknown").isPresent());
                assertArrayEquals(new byte[] {7, 8}, unknown.find("tx-unknown").orElseThrow().request());
                serverFuture.get(2, TimeUnit.SECONDS);
            }
        }
    }

    @Test
    void doesNotRecordUnknownWhenConnectionFailsBeforeWrite() throws Exception {
        int unusedPort;
        try (ServerSocket reserved = new ServerSocket(0)) {
            unusedPort = reserved.getLocalPort();
        }
        CpfTcpUnknownResultStore unknown = new CpfTcpUnknownResultStore(10);
        CpfTcpProperties properties = properties(unusedPort, 1);
        properties.setConnectTimeout(Duration.ofMillis(150));
        try (CpfTcpClient client = new CpfTcpClient(properties, unknown, null)) {
            IllegalStateException failure = assertThrows(
                    IllegalStateException.class,
                    () -> client.request("before-write", new byte[] {1}));
            assertTrue(failure.getMessage().contains("before write"));
            assertTrue(unknown.snapshot().isEmpty());
        }
    }

    @Test
    void connectTimeoutBeforeWriteIsDefinitiveFailureNotUnknown() {
        CpfTcpUnknownResultStore unknown = new CpfTcpUnknownResultStore(10);

        RuntimeException failure = CpfTcpClient.classifyTransportFailure(
                false, "connect-timeout", new byte[] {1}, new SocketTimeoutException("connect timed out"), unknown);

        assertTrue(failure instanceof IllegalStateException);
        assertFalse(failure instanceof CpfTcpClient.UnknownResultException);
        assertTrue(failure.getMessage().contains("before write"));
        assertTrue(unknown.snapshot().isEmpty());
    }

    @Test
    void responseTimeoutAfterWriteIsRecordedAsUnknown() {
        CpfTcpUnknownResultStore unknown = new CpfTcpUnknownResultStore(10);

        RuntimeException failure = CpfTcpClient.classifyTransportFailure(
                true, "response-timeout", new byte[] {2}, new SocketTimeoutException("read timed out"), unknown);

        assertTrue(failure instanceof CpfTcpClient.UnknownResultException);
        assertTrue(unknown.find("response-timeout").isPresent());
    }

    private static CpfTcpProperties properties(int port, int poolSize) {
        CpfTcpProperties properties = new CpfTcpProperties();
        properties.setEnabled(true);
        properties.setHost("127.0.0.1");
        properties.setPort(port);
        properties.setPoolSize(poolSize);
        properties.setConnectTimeout(Duration.ofMillis(500));
        properties.setResponseTimeout(Duration.ofMillis(500));
        properties.validate();
        return properties;
    }

    private static byte[] readFrame(Socket socket) throws Exception {
        DataInputStream input = new DataInputStream(socket.getInputStream());
        int length = input.readInt();
        return input.readNBytes(length);
    }

    private static void writeFrame(Socket socket, byte[] payload) throws Exception {
        DataOutputStream output = new DataOutputStream(socket.getOutputStream());
        output.writeInt(payload.length);
        output.write(payload);
        output.flush();
    }
    @Test
    void partialWriteFailureIsUnknownBecauseExternalSideEffectMayHaveStarted() {
        CpfTcpUnknownResultStore store = new CpfTcpUnknownResultStore(10);
        byte[] payload = "PAYLOAD".getBytes(java.nio.charset.StandardCharsets.UTF_8);

        RuntimeException failure = CpfTcpClient.classifyTransportFailure(
                true, "corr-partial", payload, new IOException("partial write"), store);

        assertTrue(failure instanceof CpfTcpClient.UnknownResultException);
        assertTrue(store.find("corr-partial").isPresent());
    }

    @Test
    void deterministicFrameValidationHappensBeforeCapacityAndProviderIo() {
        CpfTcpProperties properties = properties(65535, 1);
        properties.setFrame(CpfTcpProperties.Frame.FIXED);
        properties.setFixedLength(8);
        CpfTcpUnknownResultStore store = new CpfTcpUnknownResultStore(10);
        CpfTcpClient client = new CpfTcpClient(properties, store, null);

        IllegalArgumentException invalid = assertThrows(
                IllegalArgumentException.class,
                () -> client.request("corr-invalid", new byte[] {1, 2, 3}));
        assertTrue(invalid.getMessage().contains("before write"));
        assertTrue(store.find("corr-invalid").isEmpty());
    }

    @Test
    void fixedClockIsUsedForUnknownResultEvidence() {
        CpfTcpUnknownResultStore unknown = new CpfTcpUnknownResultStore(10);
        java.time.Clock clock = java.time.Clock.fixed(
                java.time.Instant.parse("2026-08-05T12:00:00Z"), java.time.ZoneOffset.UTC);

        RuntimeException failure = CpfTcpClient.classifyTransportFailure(
                true, "clocked-timeout", new byte[] {3},
                new SocketTimeoutException("read timed out"), unknown, clock);

        assertTrue(failure instanceof CpfTcpClient.UnknownResultException);
        assertEquals(
                java.time.Instant.parse("2026-08-05T12:00:00Z"),
                unknown.find("clocked-timeout").orElseThrow().writtenAt());
    }

}
