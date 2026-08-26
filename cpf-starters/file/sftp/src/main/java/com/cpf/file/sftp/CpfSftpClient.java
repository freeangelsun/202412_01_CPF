package com.cpf.file.sftp;

import com.cpf.core.api.context.CpfContext;
import com.cpf.core.api.context.CpfContextSnapshot;
import com.cpf.core.api.context.CpfContexts;
import com.cpf.core.api.transaction.CpfTransactionIds;

import com.cpf.file.context.*;

import com.cpf.security.api.secret.CpfSecretReference;
import com.cpf.security.api.secret.CpfSecretValue;
import com.cpf.security.secret.CpfSecretProviderRegistry;
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
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;
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
    private final Clock clock;
    private final Supplier<String> transferIdSupplier;
    private final CpfFileContextSupport fileContextSupport;

    public CpfSftpClient(
            CpfSftpProperties properties,
            ServerKeyVerifier verifier,
            JdbcCpfSftpTransferLedger ledger,
            CpfSecretProviderRegistry secrets) {
        this(properties, verifier, ledger, secrets, Clock.systemUTC(),
                () -> UUID.randomUUID().toString(), null);
    }

    public CpfSftpClient(
            CpfSftpProperties properties, ServerKeyVerifier verifier, JdbcCpfSftpTransferLedger ledger,
            CpfSecretProviderRegistry secrets, CpfFileContextSupport fileContextSupport) {
        this(properties, verifier, ledger, secrets, Clock.systemUTC(), () -> UUID.randomUUID().toString(), fileContextSupport);
    }

    CpfSftpClient(
            CpfSftpProperties properties,
            ServerKeyVerifier verifier,
            JdbcCpfSftpTransferLedger ledger,
            CpfSecretProviderRegistry secrets,
            Clock clock,
            Supplier<String> transferIdSupplier) {
        this(properties, verifier, ledger, secrets, clock, transferIdSupplier, null);
    }

    CpfSftpClient(CpfSftpProperties properties, ServerKeyVerifier verifier, JdbcCpfSftpTransferLedger ledger,
            CpfSecretProviderRegistry secrets, Clock clock, Supplier<String> transferIdSupplier, CpfFileContextSupport fileContextSupport) {
        this.properties = Objects.requireNonNull(properties, "properties");
        this.ledger = Objects.requireNonNull(ledger, "ledger");
        this.secrets = Objects.requireNonNull(secrets, "secrets");
        this.clock = Objects.requireNonNull(clock, "clock");
        this.transferIdSupplier = Objects.requireNonNull(transferIdSupplier, "transferIdSupplier");
        this.fileContextSupport = fileContextSupport;
        Objects.requireNonNull(verifier, "verifier");
        properties.validate();
        paths = new CpfSftpPathPolicy(properties.getLocalRoot(), properties.getRemoteRoot());
        ledger.recoverExpiredStarted(clock.instant().minus(properties.getStartedRecoveryAge()));
        sshClient = SshClient.setUpDefaultClient();
        sshClient.setServerKeyVerifier(verifier);
        sshClient.start();
    }

    public Result upload(Path localPath, String remotePath, boolean resume) {
        return upload(localPath, remotePath, CpfContexts.requireCurrent().transaction().transactionId(), resume);
    }

    public Result upload(
            Path localPath, String remotePath, String transactionId, boolean resume) {
        requireTransactionId(transactionId);
        Path local = paths.existingLocalFile(localPath);
        String remote = paths.remote(remotePath);
        String transferId = nextTransferId();
        CpfFileScope cpfFileScope = beginFileScope("UPLOAD", transferId, remote, transactionId, 1, null);
        try (CpfFileScope _ = cpfFileScope) {
        String remoteWork = CpfSftpUploadOutcomePolicy.remoteWorkPath(remote, transferId, resume);
        Instant startedAt = clock.instant();
        ledger.started(record(
                transferId, "UPLOAD", local.toString(), remote, "STARTED",
                0, null, transactionId, null, startedAt, null));
        boolean remoteMutationStarted = false;
        try (Context context = open()) {
            long total = Files.size(local);
            requireWithinLimit(total);
            String localChecksum = sha256(local);
            long offset = resume ? remoteSizeForResume(context.sftp(), remoteWork) : 0;
            if (offset > total) {
                throw new IllegalStateException(
                        "SFTP remote partial file is larger than the local source");
            }
            if (resume && offset == total && total > 0) {
                String remoteChecksum = sha256(context.sftp().read(remoteWork));
                if (!CpfSftpUploadOutcomePolicy.checksumMatches(
                        localChecksum, remoteChecksum)) {
                    throw new ResultUnknownException(
                            "SFTP remote file already has the source size but checksum differs");
                }
                return completeUpload(
                        transferId, local, remote, total, localChecksum,
                        transactionId, startedAt);
            }
            SftpClient.OpenMode[] modes = offset > 0
                    ? new SftpClient.OpenMode[]{SftpClient.OpenMode.Append, SftpClient.OpenMode.Write}
                    : new SftpClient.OpenMode[]{
                            SftpClient.OpenMode.Create,
                            SftpClient.OpenMode.Truncate,
                            SftpClient.OpenMode.Write};
            try (InputStream input = Files.newInputStream(local)) {
                input.skipNBytes(offset);
                // OPEN/CREATE/TRUNCATE may mutate provider state before an OutputStream is
                // returned. Cross the uncertainty boundary before invoking the provider.
                remoteMutationStarted = true;
                OutputStream opened = context.sftp().write(remoteWork, modes);
                try (OutputStream remoteOutput = opened) {
                    input.transferTo(new BoundedOutputStream(
                            remoteOutput, properties.getMaxTransferBytes() - offset));
                }
            }
            long remoteBytes = context.sftp().stat(remoteWork).getSize();
            if (remoteBytes != total) {
                throw new ResultUnknownException(
                        "SFTP upload acknowledgement size mismatch. expected="
                                + total + ", actual=" + remoteBytes);
            }
            String remoteChecksum = sha256(context.sftp().read(remoteWork));
            if (!CpfSftpUploadOutcomePolicy.checksumMatches(
                    localChecksum, remoteChecksum)) {
                throw new ResultUnknownException(
                        "SFTP upload checksum mismatch. expected="
                                + localChecksum + ", actual=" + remoteChecksum);
            }
            if (!resume) {
                // Publish only a completely verified payload. An ambiguous rename remains
                // UNKNOWN because the target may already have become visible.
                context.sftp().rename(
                        remoteWork, remote,
                        SftpClient.CopyMode.Atomic,
                        SftpClient.CopyMode.Overwrite);
            }
            return completeUpload(
                    transferId, local, remote, total, localChecksum,
                    transactionId, startedAt);
        } catch (ResultUnknownException exception) {
            ledger.completed(record(
                    transferId, "UPLOAD", local.toString(), remote, "UNKNOWN",
                    0, null, transactionId, safe(exception), startedAt, clock.instant()));
            throw failure("upload result unknown; reconcile transfer ledger before retry", exception);
        } catch (Exception exception) {
            String status = CpfSftpUploadOutcomePolicy.failureStatus(remoteMutationStarted);
            ledger.completed(record(
                    transferId, "UPLOAD", local.toString(), remote, status,
                    0, null, transactionId, safe(exception), startedAt, clock.instant()));
            String operation = remoteMutationStarted
                    ? "upload result unknown; reconcile transfer ledger before retry"
                    : "upload";
            throw failure(operation, exception);
        }
        }
    }

    private Result completeUpload(
            String transferId,
            Path local,
            String remote,
            long bytes,
            String checksum,
            String transactionId,
            Instant startedAt) {
        Result result = new Result(transferId, bytes, checksum, "COMPLETED");
        ledger.completed(record(
                transferId, "UPLOAD", local.toString(), remote, result.status(),
                result.bytes(), result.checksum(), transactionId, null,
                startedAt, clock.instant()));
        return result;
    }

    public Result download(String remotePath, Path localPath, boolean resume) {
        return download(remotePath, localPath, CpfContexts.requireCurrent().transaction().transactionId(), resume);
    }

    public Result download(
            String remotePath, Path localPath, String transactionId, boolean resume) {
        requireTransactionId(transactionId);
        String remote = paths.remote(remotePath);
        Path target = paths.localTarget(localPath);
        String transferId = nextTransferId();
        CpfFileScope cpfFileScope = beginFileScope("DOWNLOAD", transferId, remote, transactionId, 1, null);
        try (CpfFileScope _ = cpfFileScope) {
        Instant startedAt = clock.instant();
        ledger.started(record(
                transferId, "DOWNLOAD", remote, target.toString(), "STARTED",
                0, null, transactionId, null, startedAt, null));
        Path workFile = resume
                ? target
                : target.resolveSibling(target.getFileName() + ".cpf-part-" + transferId);
        boolean payloadCommitted = false;
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
            payloadCommitted = true;
            String checksum = sha256(target);
            Result result = new Result(transferId, downloadedBytes, checksum, "COMPLETED");
            ledger.completed(record(
                    transferId, "DOWNLOAD", remote, target.toString(), result.status(),
                    result.bytes(), result.checksum(), transactionId, null,
                    startedAt, clock.instant()));
            return result;
        } catch (ResultUnknownException exception) {
            ledger.completed(record(
                    transferId, "DOWNLOAD", remote, target.toString(), "UNKNOWN",
                    safeSize(workFile), null, transactionId, safe(exception),
                    startedAt, clock.instant()));
            throw failure("download result unknown; reconcile transfer ledger before retry", exception);
        } catch (Exception exception) {
            long partialBytes = safeSize(payloadCommitted ? target : workFile);
            if (!resume && !payloadCommitted) {
                deleteQuietly(workFile);
            }
            String status = payloadCommitted ? "UNKNOWN" : "FAILED";
            ledger.completed(record(
                    transferId, "DOWNLOAD", remote, target.toString(), status,
                    partialBytes, null, transactionId, safe(exception),
                    startedAt, clock.instant()));
            String operation = payloadCommitted
                    ? "download completed locally but durable ledger result is unknown"
                    : "download";
            throw failure(operation, exception);
        }
        }
    }

    public List<String> list(String remoteDirectory) {
        String remote = paths.remote(remoteDirectory);
        try (CpfFileScope _ = beginFileScope("LIST", nextTransferId(), remote, currentTransactionId(), 1, null);
                Context context = open()) {
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
        try (CpfFileScope _ = beginFileScope("MOVE", nextTransferId(), target, currentTransactionId(), 1, null);
                Context context = open()) {
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
        try (CpfFileScope _ = beginFileScope("DELETE", nextTransferId(), remote, currentTransactionId(), 1, null);
                Context context = open()) {
            context.sftp().remove(remote);
        } catch (Exception exception) {
            throw failure("delete", exception);
        }
    }

    public void verifyConnection() {
        try (Context context = open()) {
            if (context.session() == null || context.sftp() == null) {
                throw new IOException("SFTP health context is incomplete");
            }
        } catch (Exception exception) {
            throw failure("health", exception);
        }
    }


    private CpfFileScope beginFileScope(String direction, String transferId, String remote, String transactionId, int attempt, String recoveryId) {
        if (fileContextSupport == null) return null;
        CpfContext current = CpfContexts.requireCurrent();
        if (!current.transaction().transactionId().equals(transactionId)) {
            throw new SecurityException("SFTP transactionId does not match bound CPF Context");
        }
        String logical = remote == null ? null : remote.substring(Math.max(remote.lastIndexOf('/'), remote.lastIndexOf('\\')) + 1);
        CpfContextSnapshot snapshot = fileContextSupport.child(null, transferId, logical, "SFTP", null, sha256Text(remote),
                "APACHE_SSHD", direction, null, null, null, null, attempt, recoveryId);
        AutoCloseable delegate = CpfContexts.bind(snapshot);
        return () -> {
            try {
                delegate.close();
            } catch (RuntimeException failure) {
                throw failure;
            } catch (Exception failure) {
                throw new IllegalStateException("CPF file context scope close failed", failure);
            }
        };
    }

    @FunctionalInterface
    private interface CpfFileScope extends AutoCloseable {
        @Override
        void close();
    }

    private String currentTransactionId() { return CpfContexts.requireCurrent().transaction().transactionId(); }

    private static String sha256Text(String value) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(Objects.toString(value, "").getBytes(java.nio.charset.StandardCharsets.UTF_8))); }
        catch (Exception e) { throw new IllegalStateException("SHA-256 unavailable", e); }
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
        String requiredValue = Objects.requireNonNull(value, "SFTP password-secret must be provider:key");
        int separator = requiredValue.indexOf(':');
        if (separator <= 0 || separator == requiredValue.length() - 1) {
            throw new IllegalArgumentException("SFTP password-secret must be provider:key");
        }
        return new CpfSecretReference(
                requiredValue.substring(0, separator), requiredValue.substring(separator + 1));
    }

    private void requireWithinLimit(long bytes) {
        if (bytes < 0 || bytes > properties.getMaxTransferBytes()) {
            throw new IllegalArgumentException("SFTP file exceeds configured transfer limit");
        }
    }

    private static long remoteSizeForResume(SftpClient client, String remotePath) throws IOException {
        try {
            return client.stat(remotePath).getSize();
        } catch (IOException exception) {
            if (isNoSuchRemoteFile(exception)) {
                return 0;
            }
            throw exception;
        }
    }

    static boolean isNoSuchRemoteFile(Throwable failure) {
        for (Throwable current = failure; current != null; current = current.getCause()) {
            if (!current.getClass().getName().equals("org.apache.sshd.sftp.common.SftpException")) {
                continue;
            }
            try {
                Object status = current.getClass().getMethod("getStatus").invoke(current);
                return status instanceof Number && ((Number) status).intValue() == 2;
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }
        return false;
    }

    private static String sha256(Path path) throws Exception {
        try (InputStream input = Files.newInputStream(path)) {
            return sha256(input);
        }
    }

    private static String sha256(InputStream input) throws Exception {
        try (InputStream source = input) {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[65_536];
            int count;
            while ((count = source.read(buffer)) >= 0) {
                digest.update(buffer, 0, count);
            }
            return HexFormat.of().formatHex(digest.digest());
        }
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
        value = value
                .replaceAll("(?i)(password|passwd|pwd|secret|token|api[-_]?key|authorization)\\s*[:=]\\s*[^\\s,;]+", "$1=***")
                .replaceAll("(?i)bearer\\s+[A-Za-z0-9._~+/=-]+", "Bearer ***");
        return value.substring(0, Math.min(1_000, value.length()));
    }

    private static void requireTransactionId(String transactionId) {
        if (!CpfTransactionIds.isCanonical(transactionId)) {
            throw new IllegalArgumentException("SFTP transactionId는 CPF 34자리 표준 형식이어야 합니다.");
        }
    }

    private String nextTransferId() {
        String transferId = transferIdSupplier.get();
        if (transferId == null || transferId.isBlank()) {
            throw new IllegalStateException("transferIdSupplier returned a blank value");
        }
        return transferId.trim();
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

    private record Context(ClientSession session, SftpClient sftp) implements java.io.Closeable {
        @Override
        public void close() throws IOException {
            try {
                sftp.close();
            } finally {
                session.close();
            }
        }
    }

    private static final class ResultUnknownException extends IOException {
        private static final long serialVersionUID = 1L;
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
