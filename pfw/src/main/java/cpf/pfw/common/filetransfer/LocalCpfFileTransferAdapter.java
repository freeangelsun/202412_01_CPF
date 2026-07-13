package cpf.pfw.common.filetransfer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * 외부 서버 없이 streaming, temp file, rename, checksum 정책을 검증하는 LOCAL reference adapter입니다.
 */
public class LocalCpfFileTransferAdapter implements CpfFileTransferPort {
    private static final int BUFFER_SIZE = 64 * 1024;

    @Override
    public CpfFileTransferResult execute(CpfFileTransferEndpoint endpoint, CpfFileTransferRequest request) {
        if (!"LOCAL".equalsIgnoreCase(endpoint.protocol())) {
            throw new IllegalArgumentException("LOCAL adapter는 LOCAL protocol만 처리합니다.");
        }
        try {
            TransferPaths paths = resolvePaths(endpoint, request);
            if (!Files.isRegularFile(paths.source())) {
                return CpfFileTransferResult.failed(request, "원본 파일이 존재하지 않습니다.");
            }
            boolean overwrite = "Y".equalsIgnoreCase(endpoint.attributes().getOrDefault("overwriteYn", "N"));
            if (Files.exists(paths.target()) && !overwrite) {
                return new CpfFileTransferResult(
                        "DUPLICATE",
                        request.endpointCode(),
                        request.localPath(),
                        request.remotePath(),
                        request.checksum(),
                        request.fileSize(),
                        null,
                        "대상 파일이 이미 존재하며 overwrite가 허용되지 않습니다.");
            }

            Path parent = paths.target().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Path temp = paths.target().resolveSibling(paths.target().getFileName() + ".cpf.tmp");
            copyStreaming(paths.source(), temp);
            moveTemp(temp, paths.target(), overwrite);

            String sourceChecksum = sha256(paths.source());
            String targetChecksum = sha256(paths.target());
            if (!sourceChecksum.equals(targetChecksum)) {
                throw new CpfFileTransferUnknownResultException("전송 후 checksum이 일치하지 않습니다.");
            }
            return CpfFileTransferResult.success(request, targetChecksum, Files.size(paths.target()));
        } catch (CpfFileTransferUnknownResultException ex) {
            throw ex;
        } catch (IOException ex) {
            return CpfFileTransferResult.failed(request, "LOCAL 파일 전송에 실패했습니다: " + safeMessage(ex));
        }
    }

    private TransferPaths resolvePaths(CpfFileTransferEndpoint endpoint, CpfFileTransferRequest request) {
        Path remoteBase = Path.of(endpoint.remoteBasePath()).toAbsolutePath().normalize();
        Path remote = remoteBase.resolve(request.remotePath()).normalize();
        if (!remote.startsWith(remoteBase)) {
            throw new IllegalArgumentException("remotePath가 허용된 기준 경로를 벗어났습니다.");
        }
        Path local = Path.of(request.localPath()).toAbsolutePath().normalize();
        if ("DOWNLOAD".equalsIgnoreCase(request.operation())) {
            return new TransferPaths(remote, local);
        }
        return new TransferPaths(local, remote);
    }

    private void copyStreaming(Path source, Path target) throws IOException {
        try (InputStream input = Files.newInputStream(source);
             OutputStream output = Files.newOutputStream(
                     target,
                     StandardOpenOption.CREATE,
                     StandardOpenOption.TRUNCATE_EXISTING,
                     StandardOpenOption.WRITE)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = input.read(buffer)) >= 0) {
                if (read > 0) {
                    output.write(buffer, 0, read);
                }
            }
        }
    }

    private void moveTemp(Path temp, Path target, boolean overwrite) throws IOException {
        StandardCopyOption[] atomicOptions = overwrite
                ? new StandardCopyOption[]{StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING}
                : new StandardCopyOption[]{StandardCopyOption.ATOMIC_MOVE};
        try {
            Files.move(temp, target, atomicOptions);
        } catch (AtomicMoveNotSupportedException ex) {
            StandardCopyOption[] fallbackOptions = overwrite
                    ? new StandardCopyOption[]{StandardCopyOption.REPLACE_EXISTING}
                    : new StandardCopyOption[]{};
            Files.move(temp, target, fallbackOptions);
        }
    }

    private String sha256(Path path) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream input = Files.newInputStream(path)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int read;
                while ((read = input.read(buffer)) >= 0) {
                    if (read > 0) {
                        digest.update(buffer, 0, read);
                    }
                }
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 checksum을 계산할 수 없습니다.", ex);
        }
    }

    private String safeMessage(IOException ex) {
        return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
    }

    private record TransferPaths(Path source, Path target) {
    }
}
