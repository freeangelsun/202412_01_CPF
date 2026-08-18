package com.cpf.integration.tcp.internal;

import static org.junit.jupiter.api.Assertions.*;

import com.cpf.integration.resilience.api.CpfResilienceCallContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import com.cpf.foundation.id.spi.CpfExecutionIdGenerator;
import com.cpf.integration.resilience.api.CpfResilienceExecutor;
import com.cpf.integration.resilience.api.CpfResilienceOutcome;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketAddress;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

class CpfResilientTcpClientTest {
    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-08-05T12:00:00Z"), ZoneOffset.UTC);

    private AutoCloseable contextScope;

    @BeforeEach
    void bindContext() {
        CpfExecutionIdGenerator ids = new CpfExecutionIdGenerator() {
            @Override public String newExecutionId() { return "tcp-execution-1"; }
            @Override public String newSegmentId() { return "tcp-segment-1"; }
        };
        CpfContextExecutionFactory factory = new CpfContextExecutionFactory(
                () -> "tx-tcp", ids, () -> LocalDate.of(2026, 8, 5), CLOCK);
        contextScope = CpfContexts.bind(CpfContextSnapshot.capture(
                factory.newRoot(null, "tcp.test", null, null, CLOCK.instant().plusSeconds(60)),
                CLOCK.instant()));
    }

    @AfterEach
    void clearContext() throws Exception {
        if (contextScope != null) contextScope.close();
    }

    @Test
    void writeRetryRequiresIdempotencyKeyBeforeExecutorInvocation() {
        AtomicReference<CpfResilienceCallContext> captured = new AtomicReference<>();
        CpfResilientTcpClient client = client(captured, new SuccessfulSocket());

        assertThrows(
                IllegalArgumentException.class,
                () -> client.exchangeWrite(
                        "tcp.write", " ", "localhost", 1234, new byte[] {1},
                        64, 100, 100, true));
        assertNull(captured.get());
    }

    @Test
    void writeContextCarriesExplicitSemanticsAndImmutableRequestSnapshot() {
        AtomicReference<CpfResilienceCallContext> captured = new AtomicReference<>();
        SuccessfulSocket socket = new SuccessfulSocket();
        CpfResilientTcpClient client = client(captured, socket);
        byte[] request = new byte[] {7};

        CpfResilienceOutcome<byte[]> outcome = client.exchangeWrite(
                "tcp.write", "idem-2", "localhost", 1234, request,
                64, 100, 100, true);
        request[0] = 9;

        assertArrayEquals(new byte[] {3}, outcome.value());
        assertArrayEquals(new byte[] {7}, socket.written.toByteArray());
        assertEquals(CpfResilienceCallContext.OperationKind.WRITE, captured.get().operationKind());
        assertTrue(captured.get().timeoutRetryAllowed());
        assertEquals(Instant.parse("2026-08-05T12:00:00Z"), captured.get().requestedAt());
    }

    @Test
    void compatibilityEntryPointIsFailClosedForTimeoutRetry() {
        AtomicReference<CpfResilienceCallContext> captured = new AtomicReference<>();
        CpfResilientTcpClient client = client(captured, new SuccessfulSocket());

        client.exchange("tcp.compat", null, "localhost", 1234, new byte[0],
                64, 100, 100);

        assertEquals(CpfResilienceCallContext.OperationKind.UNKNOWN, captured.get().operationKind());
        assertFalse(captured.get().timeoutRetryAllowed());
    }

    @Test
    void connectionFailureIsDeterministicPreDispatchFailure() {
        CpfResilientTcpClient client = client(new AtomicReference<>(), new ConnectFailSocket());

        assertThrows(
                CpfResilientTcpClient.CpfTcpPreDispatchException.class,
                () -> client.exchangeRead("tcp.read", "localhost", 1234,
                        new byte[] {1}, 64, 100, 100));
    }

    @Test
    void writeFailureIsUnknownBecauseDispatchMayBePartial() {
        CpfResilientTcpClient client = client(new AtomicReference<>(), new WriteFailSocket());

        assertThrows(
                CpfResilientTcpClient.CpfTcpUnknownResultException.class,
                () -> client.exchangeWrite("tcp.write", "idem-5", "localhost", 1234,
                        new byte[] {1}, 64, 100, 100, false));
    }

    @Test
    void oversizedResponseAfterDispatchIsUnknown() {
        CpfResilientTcpClient client = client(new AtomicReference<>(),
                new SuccessfulSocket(new byte[] {1, 2, 3}));

        assertThrows(
                CpfResilientTcpClient.CpfTcpUnknownResultException.class,
                () -> client.exchangeRead("tcp.read", "localhost", 1234,
                        new byte[] {1}, 2, 100, 100));
    }

    private static CpfResilientTcpClient client(
            AtomicReference<CpfResilienceCallContext> captured, Socket socket) {
        CpfResilienceExecutor executor = new CpfResilienceExecutor() {
            @Override
            public <T> CpfResilienceOutcome<T> execute(
                    CpfResilienceCallContext context, Supplier<T> operation) {
                captured.set(context);
                return new CpfResilienceOutcome<>(
                        CpfResilienceOutcome.Status.SUCCESS, operation.get(), null, 1, 0, CLOCK.instant());
            }

            @Override
            public <T> CpfResilienceOutcome<T> reconcile(
                    CpfResilienceCallContext context, Supplier<T> probe) {
                return execute(context, probe);
            }
        };
        return new CpfResilientTcpClient(executor, CLOCK, () -> socket);
    }

    private static class SuccessfulSocket extends Socket {
        final ByteArrayOutputStream written = new ByteArrayOutputStream();
        private final byte[] response;

        SuccessfulSocket() {
            this(new byte[] {3});
        }

        SuccessfulSocket(byte[] response) {
            this.response = response;
        }

        @Override public void connect(SocketAddress endpoint, int timeout) throws IOException {}
        @Override public void setSoTimeout(int timeout) {}
        @Override public OutputStream getOutputStream() { return written; }
        @Override public InputStream getInputStream() { return new ByteArrayInputStream(response); }
        @Override public void close() {}
    }

    private static final class ConnectFailSocket extends SuccessfulSocket {
        @Override public void connect(SocketAddress endpoint, int timeout) throws IOException {
            throw new IOException("connect failed");
        }
    }

    private static final class WriteFailSocket extends SuccessfulSocket {
        @Override public OutputStream getOutputStream() {
            return new OutputStream() {
                @Override public void write(int value) throws IOException {
                    throw new IOException("partial write");
                }
            };
        }
    }
}
