package com.cpf.starter.sftp;

import com.cpf.core.api.security.secret.CpfSecretReference;
import com.cpf.core.api.security.secret.CpfSecretValue;
import com.cpf.starter.secret.CpfSecretProviderRegistry;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.ServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.sftp.client.SftpClient;
import org.apache.sshd.sftp.client.SftpClientFactory;

/**
 * CPF SFTP convenience API with root isolation, secret indirection, durable ledger and
 * explicit result-unknown handling.
 */
public final class CpfSftpClient implements AutoCloseable {
    private final CpfSftpProperties properties;
    private final JdbcCpfSftpTransferLedger ledger;
    private final CpfSecretProviderRegistry secrets;
    private final SshClient sshClient;
    private final CpfSftpPathPolicy paths;

    public CpfSftpClient(
            CpfSftpProperties properties,
            ServerKeyVerifier verifier,
            JdbcCpfSftpTransferLedger ledger,
            CpfSecretProviderRegistry secrets) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.ledger = Objects.requireNonNull(ledger, "ledger");
        this.secrets = Objects.requireNonNull(secrets, "secrets");
        Objects.requireNonNull(verifier, "verifier");
        properties.validate();
        paths = new CpfSftpPathPolicy(properties.getLocalRoot(), properties.getRemoteRoot());
        sshClient = SshClient.setUpDefaultClient();
        sshClient.setServerKeyVerifier(verifier);
        sshClient.start();
    }

    public Result upload(
            Path localPath, String remotePath, String transactionId, boolean resume) {
        Path local = paths.existingLocalFile(localPath);
        String remote = paths.remote(remotePath);
        String transferId = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        ledger.started(record(
                transferId, "UPLOAD", local.toString(), remote, "STARTED",
                0, null, transactionId, null, startedAt, null));
        boolean remoteMutationStarted = false;
        try (Context context = open()) {
            long total = Files.size(local);
            requireWithinLimit(total);
            long offset = resume ? remoteSize(context.sftp(), remote) : 0;
            if (offset > total) {
                throw new IllegalStateException(
                        "SFTP remote partial file is larger than the local source");
            }
            if (resume && offset == total && total > 0) {
                throw new ResultUnknownException(
                        "SFTP remote file already has the source size but content is not verified");
            }
            SftpClient.OpenMode[] modes = offset > 0
                    ? new SftpClient.OpenMode[]{SftpClient.OpenMode.Append, SftpClient.OpenMode.Write}
                    : new SftpClient.OpenMode[]{
                            SftpClient.OpenMode.Create,
                            SftpClient.OpenMode.Truncate,
                            SftpClient.OpenMode.Write};
            try (InputStream input = Files.newInputStream(local);
                    OutputStream remoteOutput = context.sftp().write(remote, modes)) {
                remoteMutationStarted = true;
                input.skipNBytes(offset);
                input.transferTo(new BoundedOutputStream(
                        remoteOutput, properties.getMaxTransferBytes() - offset));
            }
            long remoteBytes = context.sftp().stat(remote).getSize();
            if (remoteBytes != total) {
                throw new ResultUnknownException(
                        "SFTP upload acknowledgement size mismatch. expected="
                                + total + ", actual=" + remoteBytes);
            }
            String checksum = sha256(local);
            Result result = new Result(transferId, total, checksum, "COMPLETED");
            ledger.completed(record(
                    transferId, "UPLOAD", local.toString(), remote, result.status(),
                    result.bytes(), result.checksum(), transactionId, null,
                    startedAt, Instant.now()));
            return result;
        } catch (ResultUnknownException exception) {
            ledger.completed(record(
                    transferId, "UPLOAD", local.toString(), remote, "UNKNOWN",
                    0, null, transactionId, safe(exception), startedAt, Instant.now()));
            throw failure("upload result unknown; reconcile transfer ledger before retry", exception);
        } catch (Exception exception) {
            String status = remoteMutationStarted ? "UNKNOWN" : "FAILED";
            ledger.completed(record(
                    transferId, "UPLOAD", local.toString(), remote, status,
                    0, null, transactionId, safe(exception), startedAt, Instant.now()));
            String operation = remoteMutationStarted
                    ? "upload result unknown; reconcile transfer ledger before retry"
                    : "upload";
            throw failure(operation, exception);
        }
    }

    public Result download(
            String remotePath, Path localPath, String transactionId, boolean resume) {
        String remote = paths.remote(remotePath);
        Path target = paths.localTarget(localPath);
        String transferId = UUID.randomUUID().toString();
        Instant startedAt = Instant.now();
        ledger.started(record(
                transferId, "DOWNLOAD", remote, target.toString(), "STARTED",
                0, null, transactionId, null, startedAt, null));
        Path workFile = resume
                ? target
                : target.resolveSibling(target.getFileName() + ".cpf-part-" + transferId);
        try (Context context = open()) {
            long remoteBytes = context.sftp().stat(remote).getSize();
            requireWithinLimit(remoteBytes);
            long offset = resume && Files.exists(workFile) ? Files.size(workFile) : 0;
            if (offset > remoteBytes) {
                throw new IllegalStateException(
                        "SFTP local partial file is larger than the remote source");
            }
            if (resume && offset == remoteBytes && remoteBytes > 0) {
                throw new ResultUnknownException(
                        "SFTP local file already has the remote size but content is not verified");
            }
            try (InputStream remoteInput = context.sftp().read(remote);
                    OutputStream output = Files.newOutputStream(
                            workFile,
                            offset > 0
                                    ? new StandardOpenOption[]{
                                            StandardOpenOption.CREATE,
                                            StandardOpenOption.APPEND}
                                    : new StandardOpenOption[]{
                                            StandardOpenOption.CREATE,
                                            StandardOpenOption.TRUNCATE_EXISTING})) {
                remoteInput.skipNBytes(offset);
                new BoundedInputStream(
                        remoteInput, properties.getMaxTransferBytes() - offset)
                        .transferTo(output);
            }
            long downloadedBytes = Files.size(workFile);
            if (downloadedBytes != remoteBytes) {
                throw new ResultUnknownException(
                        "SFTP download size mismatch. expected="
                                + remoteBytes + ", actual=" + downloadedBytes);
            }
            if (!resume) {
                moveAtomically(workFile, target);
            }
            String checksum = sha256(target);
            Result result = new Result(transferId, downloadedBytes, checksum, "COMPLETED");
            ledger.completed(record(
                    transferId, "DOWNLOAD", remote, target.toString(), result.status(),
                    result.bytes(), result.checksum(), transactionId, null,
                    startedAt, Instant.now()));
            return result;
        } catch (ResultUnknownException exception) {
            ledger.completed(record(
                    transferId, "DOWNLOAD", remote, target.toString(), "UNKNOWN",
                    safeSize(workFile), null, transactionId, safe(exception),
                    startedAt, Instant.now()));
            throw failure("download result unknown; reconcile transfer ledger before retry", exception);
        } catch (Exception exception) {
            long partialBytes = safeSize(workFile);
            if (!resume) {
                deleteQuietly(workFile);
            }
            ledger.completed(record(
                    transferId, "DOWNLOAD", remote, target.toString(), "FAILED",
                    partialBytes, null, transactionId, safe(exception),
                    startedAt, Instant.now()));
            throw failure("download", exception);
        }
    }

    public List<String> list(String remoteDirectory) {
        String remote = paths.remote(remoteDirectory);
        try (Context context = open()) {
            List<String> result = new ArrayList<>();
            for (var entry : context.sftp().readDir(remote)) {
                if (!".".equals(entry.getFilename()) && !"..".equals(entry.getFilename())) {
                    result.add(entry.getFilename());
                }
            }
            return List.copyOf(result);
        } catch (Exception exception) {
            throw failure("list", exception);
        }
    }

    public void move(String sourcePath, String targetPath) {
        String source = paths.remote(sourcePath);
        String target = paths.remote(targetPath);
        try (Context context = open()) {
            context.sftp().rename(
                    source, target,
                    SftpClient.CopyMode.Atomic,
                    SftpClient.CopyMode.Overwrite);
        } catch (Exception exception) {
            throw failure("move", exception);
        }
    }

    public void delete(String remotePath) {
        String remote = paths.remote(remotePath);
        try (Context context = open()) {
            context.sftp().remove(remote);
        } catch (Exception exception) {
            throw failure("delete", exception);
        }
    }

    public void verifyConnection() {
        try (Context ignored = open()) {
            // Authentication and subsystem creation are the health contract.
        } catch (Exception exception) {
            throw failure("health", exception);
        }
    }

    private Context open() throws IOException {
        CpfSecretReference reference = parseSecret(properties.getPasswordSecret());
        try (CpfSecretValue value = secrets.resolve(reference)) {
            char[] secret = value.copy();
            try {
                ClientSession session = sshClient
                        .connect(
                                properties.getUsername(),
                                properties.getHost(),
                                properties.getPort())
                        .verify(properties.getConnectTimeout())
                        .getSession();
                try {
                    session.addPasswordIdentity(new String(secret));
                    session.auth().verify(properties.getOperationTimeout());
                    return new Context(
                            session,
                            SftpClientFactory.instance().createSftpClient(session));
                } catch (Exception exception) {
                    session.close();
                    throw exception;
                }
            } finally {
                Arrays.fill(secret, '\0');
            }
        } catch (RuntimeException exception) {
            throw new IOException("SFTP connection/authentication failed", exception);
        }
    }

    private static CpfSecretReference parseSecret(String value) {
        int separator = value == null ? -1 : value.indexOf(':');
        if (separator <= 0 || separator == value.length() - 1) {
            throw new IllegalArgumentException("SFTP password-secret must be provider:key");
        }
        return new CpfSecretReference(
                value.substring(0, separator), value.substring(separator + 1));
    }

    private void requireWithinLimit(long bytes) {
        if (bytes < 0 || bytes > properties.getMaxTransferBytes()) {
            throw new IllegalArgumentException("SFTP file exceeds configured transfer limit");
        }
    }

    private static long remoteSize(SftpClient client, String remotePath) {
        try {
            return client.stat(remotePath).getSize();
        } catch (IOException exception) {
            return 0;
        }
    }

    private static String sha256(Path path) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        try (InputStream input = Files.newInputStream(path)) {
            byte[] buffer = new byte[65_536];
            int count;
            while ((count = input.read(buffer)) >= 0) {
                digest.update(buffer, 0, count);
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static void moveAtomically(Path source, Path target) throws IOException {
        try {
            Files.move(
                    source, target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // The durable ledger retains the failure for operator cleanup.
        }
    }

    private static long safeSize(Path path) {
        try {
            return Files.exists(path) ? Files.size(path) : 0;
        } catch (IOException ignored) {
            return 0;
        }
    }

    private static RuntimeException failure(String operation, Exception exception) {
        return new IllegalStateException(
                "SFTP " + operation + " failed; inspect/reconcile the transfer ledger before retry",
                exception);
    }

    private static String safe(Exception exception) {
        String message = exception.getMessage();
        String value = message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
        return value.substring(0, Math.min(1_000, value.length()));
    }

    private static CpfSftpTransferRecord record(
            String transferId,
            String direction,
            String source,
            String target,
            String status,
            long bytes,
            String checksum,
            String transactionId,
            String error,
            Instant startedAt,
            Instant completedAt) {
        return new CpfSftpTransferRecord(
                transferId, direction, source, target, status, bytes, checksum,
                transactionId, error, startedAt, completedAt);
    }

    @Override
    public void close() {
        sshClient.stop();
    }

    public record Result(String transferId, long bytes, String checksum, String status) {
        public Result {
            Objects.requireNonNull(transferId, "transferId");
            Objects.requireNonNull(status, "status");
        }
    }

    private record Context(ClientSession session, SftpClient sftp) implements AutoCloseable {
        @Override
        public void close() throws Exception {
            try {
                sftp.close();
            } finally {
                session.close();
            }
        }
    }

    private static final class ResultUnknownException extends IOException {
        private ResultUnknownException(String message) {
            super(message);
        }
    }

    private static final class BoundedOutputStream extends FilterOutputStream {
        private final long maximumBytes;
        private long count;

        private BoundedOutputStream(OutputStream output, long maximumBytes) {
            super(output);
            this.maximumBytes = maximumBytes;
        }

        @Override
        public void write(int value) throws IOException {
            check(1);
            super.write(value);
        }

        @Override
        public void write(byte[] value, int offset, int length) throws IOException {
            check(length);
            super.write(value, offset, length);
        }

        private void check(int increment) throws IOException {
            count += increment;
            if (count > maximumBytes) {
                throw new IOException("SFTP transfer limit exceeded");
            }
        }
    }

    private static final class BoundedInputStream extends FilterInputStream {
        private final long maximumBytes;
        private long count;

        private BoundedInputStream(InputStream input, long maximumBytes) {
            super(input);
            this.maximumBytes = maximumBytes;
        }

        @Override
        public int read() throws IOException {
            int value = super.read();
            if (value >= 0) {
                check(1);
            }
            return value;
        }

        @Override
        public int read(byte[] value, int offset, int length) throws IOException {
            int count = super.read(value, offset, length);
            if (count > 0) {
                check(count);
            }
            return count;
        }

        private void check(int increment) throws IOException {
            count += increment;
            if (count > maximumBytes) {
                throw new IOException("SFTP transfer limit exceeded");
            }
        }
    }
}
