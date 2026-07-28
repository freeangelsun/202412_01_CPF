package com.cpf.gateway.transport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * retry/failover 시 매 시도마다 새 InputStream을 제공하는 요청 본문입니다.
 * 작은 본문은 메모리에 두고 임계치를 넘으면 권한이 제한된 임시파일로 전환합니다.
 */
public final class CpfGatewayReplayableBody implements AutoCloseable {
    private final byte[] memory;
    private final Path file;
    private final long length;
    private final AtomicBoolean closed = new AtomicBoolean();

    private CpfGatewayReplayableBody(byte[] memory, Path file, long length) {
        this.memory = memory;
        this.file = file;
        this.length = length;
    }

    public static CpfGatewayReplayableBody capture(
            InputStream source,
            long declaredLength,
            CpfGatewayTransferPolicy policy) {
        if (declaredLength > policy.maxRequestBytes()) {
            throw new CpfGatewayPayloadTooLargeException(policy.maxRequestBytes());
        }
        if (source == null) return new CpfGatewayReplayableBody(new byte[0], null, 0L);

        ByteArrayOutputStream memory = new ByteArrayOutputStream(
                (int) Math.min(Math.max(0L, declaredLength), policy.memoryThresholdBytes()));
        Path file = null;
        OutputStream destination = memory;
        long total = 0L;
        byte[] buffer = new byte[policy.ioBufferBytes()];
        try {
            int read;
            while ((read = source.read(buffer)) != -1) {
                if (total + read > policy.maxRequestBytes()) {
                    throw new CpfGatewayPayloadTooLargeException(policy.maxRequestBytes());
                }
                if (file == null && total + read > policy.memoryThresholdBytes()) {
                    file = createSecureTempFile(policy.tempDirectory());
                    OutputStream fileOutput = Files.newOutputStream(file);
                    memory.writeTo(fileOutput);
                    destination = fileOutput;
                }
                destination.write(buffer, 0, read);
                total += read;
            }
            destination.flush();
            if (destination != memory) destination.close();
            return file == null
                    ? new CpfGatewayReplayableBody(memory.toByteArray(), null, total)
                    : new CpfGatewayReplayableBody(null, file, total);
        } catch (CpfGatewayPayloadTooLargeException ex) {
            closeAndDelete(destination, memory, file);
            throw ex;
        } catch (IOException ex) {
            closeAndDelete(destination, memory, file);
            throw new UncheckedIOException("Gateway 요청 본문을 안전하게 보관하지 못했습니다.", ex);
        }
    }

    public InputStream openStream() {
        if (closed.get()) throw new IllegalStateException("이미 정리된 Gateway 요청 본문입니다.");
        try {
            return file == null ? new ByteArrayInputStream(memory) : Files.newInputStream(file);
        } catch (IOException ex) {
            throw new UncheckedIOException("Gateway 요청 본문을 다시 열지 못했습니다.", ex);
        }
    }

    public long length() {
        return length;
    }

    public boolean fileBacked() {
        return file != null;
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true) || file == null) return;
        try {
            Files.deleteIfExists(file);
        } catch (IOException ex) {
            file.toFile().deleteOnExit();
        }
    }

    private static Path createSecureTempFile(Path directory) throws IOException {
        Files.createDirectories(directory);
        Path path = Files.createTempFile(directory, "cpf-gateway-", ".body");
        try {
            Files.setPosixFilePermissions(path, Set.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE));
        } catch (UnsupportedOperationException ignored) {
            // Windows에서는 createTempFile 기본 ACL을 사용합니다.
        }
        return path;
    }

    private static void closeAndDelete(OutputStream destination, ByteArrayOutputStream memory, Path file) {
        if (destination != memory) {
            try { destination.close(); } catch (IOException ignored) { }
        }
        if (file != null) {
            try { Files.deleteIfExists(file); } catch (IOException ignored) { file.toFile().deleteOnExit(); }
        }
    }
}
