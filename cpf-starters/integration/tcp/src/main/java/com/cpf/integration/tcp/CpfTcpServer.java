package com.cpf.integration.tcp;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.foundation.execution.CpfContextExecutionFactory;
import java.io.EOFException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocket;

/** TCP server boundary. Managed mode creates one canonical CPF root per inbound frame. */
public final class CpfTcpServer implements AutoCloseable {
    private final CpfTcpProperties properties;
    private final CpfTcpFrameCodec codec;
    private final Function<byte[], byte[]> handler;
    private final ExecutorService workers;
    private final CpfContextExecutionFactory contextFactory;
    private final CpfTcpTlsContextProvider tls;
    private volatile boolean running;
    private ServerSocket server;

    public CpfTcpServer(CpfTcpProperties properties, Function<byte[], byte[]> handler, CpfTcpTlsContextProvider tls) {
        this(properties, handler, tls, null);
    }

    public CpfTcpServer(
            CpfTcpProperties properties,
            Function<byte[], byte[]> handler,
            CpfTcpTlsContextProvider tls,
            CpfContextExecutionFactory contextFactory) {
        this.properties = properties;
        this.codec = new CpfTcpFrameCodec(properties);
        this.handler = handler;
        this.tls = tls;
        this.contextFactory = contextFactory;
        this.workers = Executors.newFixedThreadPool(properties.getPoolSize(), Thread.ofVirtual().factory());
    }

    public synchronized void start() {
        if (running) return;
        try {
            ServerSocketFactory factory = properties.isTls()
                    ? tls.current().getServerSocketFactory() : ServerSocketFactory.getDefault();
            server = factory.createServerSocket(properties.getPort());
            if (server instanceof SSLServerSocket ssl) ssl.setNeedClientAuth(properties.isMutualTls());
            running = true;
            Thread.ofVirtual().name("cpf-tcp-accept").start(this::acceptLoop);
        } catch (IOException ex) {
            throw new IllegalStateException("TCP server start failed", ex);
        }
    }

    private void acceptLoop() {
        while (running) {
            try {
                Socket socket = server.accept();
                workers.submit(() -> handle(socket));
            } catch (IOException ex) {
                if (running) throw new IllegalStateException(ex);
            }
        }
    }

    private void handle(Socket socket) {
        try (socket) {
            socket.setSoTimeout((int) properties.getIdleTimeout().toMillis());
            while (running && !socket.isClosed()) {
                byte[] request = codec.read(socket.getInputStream());
                byte[] response;
                if (contextFactory != null) {
                    String peer = socket.getRemoteSocketAddress() == null
                            ? "unknown" : socket.getRemoteSocketAddress().toString();
                    CpfContext root = contextFactory.newRoot(new CpfContextExecutionFactory.RootSpec(
                            null,
                            "tcp.inbound",
                            CpfContext.CpfExecutionType.INTEGRATION,
                            CpfContext.CpfTransactionOriginKind.INTEGRATION,
                            peer,
                            null,
                            null,
                            null,
                            null,
                            null));
                    CpfContextSnapshot snapshot = CpfContextSnapshot.capture(root);
                    try (AutoCloseable ignored = CpfContexts.bind(snapshot)) {
                        response = handler.apply(request);
                    }
                } else {
                    response = handler.apply(request);
                }
                if (response != null) codec.write(socket.getOutputStream(), response);
            }
        } catch (EOFException | SocketTimeoutException ignored) {
            // Peer closed or idle timeout: deterministic connection lifecycle end.
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("TCP context scope close failed", ex);
        }
    }

    @Override
    public synchronized void close() {
        running = false;
        if (server != null) try { server.close(); } catch (IOException ignored) { }
        workers.shutdown();
        try {
            workers.awaitTermination(Math.max(1, properties.getResponseTimeout().toMillis()), TimeUnit.MILLISECONDS);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            workers.shutdownNow();
        }
    }
}
