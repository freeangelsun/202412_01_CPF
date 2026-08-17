package com.cpf.file.common.filetransfer;

import com.cpf.file.api.filetransfer.CpfFileExchangeHistoryRecord;
import com.cpf.file.api.filetransfer.CpfFileExchangeOperations;
import com.cpf.file.api.filetransfer.CpfFileResult;
import com.cpf.file.api.filetransfer.CpfRemoteCommandPlan;
import com.cpf.file.spi.filetransfer.CpfFileTransferEndpoint;
import com.cpf.file.spi.filetransfer.CpfFileTransferRequest;
import com.cpf.file.spi.filetransfer.CpfFileTransferResult;
import com.cpf.security.api.CpfCredentialRef;
import com.cpf.file.sftp.CpfSftpClient;
import com.cpf.core.api.transaction.CpfTransactionIds;
import com.cpf.foundation.id.DefaultCpfTransactionIdGenerator;
import com.cpf.foundation.id.spi.CpfTransactionIdGenerator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * 로컬 파일 helper와 파일전송·원격 명령 계획을 CPF 기술 경계에서 제공하는 gateway입니다.
 *
 * <p>운영 원격 전송은 {@link CpfFileTransferPort}의 protocol adapter가 담당합니다. 기본 gateway는
 * 외부 credential 없이 로컬 전송을 실제 실행하고 원격 요청은 안전한 계획으로만 반환합니다.</p>
 */
@Component
public class CpfFileExchangeGateway implements CpfFileExchangeOperations {
    private static final int MAX_HISTORY = 200;

    private final Path baseDir;
    private final LocalCpfFileTransferAdapter localAdapter = new LocalCpfFileTransferAdapter();
    private final ObjectProvider<CpfSftpClient> sftpClientProvider;
    private final CpfTransactionIdGenerator transactionIds;
    private final Deque<CpfFileExchangeHistoryRecord> history = new ConcurrentLinkedDeque<>();

    @Autowired
    public CpfFileExchangeGateway(
            Environment environment,
            ObjectProvider<CpfSftpClient> sftpClientProvider,
            CpfTransactionIdGenerator transactionIds) {
        String configured = environment.getProperty(
                "cpf.filetransfer.base-dir",
                "${java.io.tmpdir}/cpf-file-exchange");
        this.baseDir = Path.of(configured.replace("${java.io.tmpdir}", System.getProperty("java.io.tmpdir")))
                .toAbsolutePath()
                .normalize();
        this.sftpClientProvider = sftpClientProvider;
        this.transactionIds = java.util.Objects.requireNonNull(transactionIds, "transactionIds");
    }

    /** Source compatibility용 생성자이며 제품 Runtime은 Foundation transactionId Generator Bean을 주입합니다. */
    @Deprecated(forRemoval = false)
    public CpfFileExchangeGateway(Environment environment, ObjectProvider<CpfSftpClient> sftpClientProvider) {
        this(environment, sftpClientProvider,
                new DefaultCpfTransactionIdGenerator("FLE", Clock.systemUTC()));
    }

    public Path writeText(String relativePath, String contents, String requestUser) {
        Path target = resolveSafePath(relativePath);
        try {
            Files.createDirectories(target.getParent());
            Files.writeString(target, contents == null ? "" : contents, StandardCharsets.UTF_8);
            record("LOCAL_WRITE", "LOCAL", "WRITE", true, true, null,
                    relativePath, target.toString(), requestUser, "로컬 파일 쓰기를 완료했습니다.");
            return target;
        } catch (IOException ex) {
            record("LOCAL_WRITE", "LOCAL", "WRITE", true, false, null,
                    relativePath, target.toString(), requestUser, safeMessage(ex));
            throw new IllegalStateException("로컬 파일 쓰기에 실패했습니다: " + target, ex);
        }
    }

    public String readText(String relativePath, String requestUser) {
        Path target = resolveSafePath(relativePath);
        try {
            String contents = Files.readString(target, StandardCharsets.UTF_8);
            record("LOCAL_READ", "LOCAL", "READ", true, true, null,
                    relativePath, target.toString(), requestUser, "로컬 파일 읽기를 완료했습니다.");
            return contents;
        } catch (IOException ex) {
            record("LOCAL_READ", "LOCAL", "READ", true, false, null,
                    relativePath, target.toString(), requestUser, safeMessage(ex));
            throw new IllegalStateException("로컬 파일 읽기에 실패했습니다: " + target, ex);
        }
    }

    public List<String> list(String relativeDirectory, String requestUser) {
        Path target = resolveSafePath(hasText(relativeDirectory) ? relativeDirectory : ".");
        try (var stream = Files.list(target)) {
            List<String> files = stream
                    .map(path -> baseDir.relativize(path).toString().replace('\\', '/'))
                    .sorted()
                    .toList();
            record("LOCAL_LIST", "LOCAL", "LIST", true, true, null,
                    relativeDirectory, target.toString(), requestUser, "로컬 파일 목록을 조회했습니다.");
            return files;
        } catch (IOException ex) {
            record("LOCAL_LIST", "LOCAL", "LIST", true, false, null,
                    relativeDirectory, target.toString(), requestUser, safeMessage(ex));
            throw new IllegalStateException("로컬 파일 목록 조회에 실패했습니다: " + target, ex);
        }
    }

    @Override
    public CpfFileResult transfer(
            String protocol,
            String direction,
            String host,
            int port,
            String credentialId,
            String localPath,
            String remotePath,
            String requestUser) {
        String resolvedProtocol = requiredText(protocol, "protocol").toUpperCase(Locale.ROOT);
        String resolvedDirection = hasText(direction) ? direction.toUpperCase(Locale.ROOT) : "UPLOAD";
        CpfFileTransferRequest request = new CpfFileTransferRequest(
                null,
                null,
                "CMN_COMPAT",
                resolvedDirection,
                localPath,
                remotePath,
                null,
                0L,
                Map.of("requestUser", defaultUser(requestUser)));
        if (!"LOCAL".equals(resolvedProtocol)) {
            if (!"SFTP".equals(resolvedProtocol)) {
                throw new IllegalArgumentException("지원하지 않는 파일전송 protocol입니다: " + resolvedProtocol);
            }
            CpfSftpClient client = sftpClientProvider == null ? null : sftpClientProvider.getIfAvailable();
            if (client == null) {
                record("REMOTE_TRANSFER", resolvedProtocol, resolvedDirection, false, false, host,
                        localPath, remotePath, requestUser, "SFTP Starter가 활성화되지 않아 실행을 거부했습니다.");
                throw new IllegalStateException("SFTP 전송을 실행하려면 cpf.integration.sftp.enabled=true와 유효한 Credential 설정이 필요합니다.");
            }
            try {
                String transactionId = CpfTransactionIds.requireCanonical(transactionIds.newTransactionId());
                CpfSftpClient.Result executed;
                if ("DOWNLOAD".equals(resolvedDirection)) {
                    executed = client.download(remotePath, resolveSafePath(localPath), transactionId, true);
                } else if ("UPLOAD".equals(resolvedDirection)) {
                    executed = client.upload(resolveSafePath(localPath), remotePath, transactionId, true);
                } else {
                    throw new IllegalArgumentException("지원하지 않는 파일전송 direction입니다: " + resolvedDirection);
                }
                CpfFileResult result = new CpfFileResult(
                        executed.status(),
                        request.endpointCode(),
                        localPath,
                        remotePath,
                        executed.checksum(),
                        executed.bytes(),
                        Instant.now(),
                        "SFTP 전송을 완료했습니다. transferId=" + executed.transferId());
                record("REMOTE_TRANSFER", resolvedProtocol, resolvedDirection, true, true, host,
                        localPath, remotePath, requestUser, result.detail());
                return result;
            } catch (RuntimeException ex) {
                record("REMOTE_TRANSFER", resolvedProtocol, resolvedDirection, true, false, host,
                        localPath, remotePath, requestUser, safeMessage(ex));
                throw ex;
            }
        }

        CpfFileTransferEndpoint endpoint = new CpfFileTransferEndpoint(
                request.endpointCode(),
                "LOCAL",
                "localhost",
                0,
                baseDir.resolve("remote").toString(),
                new CpfCredentialRef("file-transfer", defaultCredential(credentialId), "latest", "local-reference"),
                Duration.ofSeconds(30),
                Map.of("localBasePath", baseDir.toString(), "overwriteYn", "Y"));
        CpfFileTransferResult result = localAdapter.execute(endpoint, request);
        record("LOCAL_TRANSFER", "LOCAL", resolvedDirection, true,
                "SUCCESS".equals(result.status()), null, localPath, remotePath, requestUser, result.detail());
        return new CpfFileResult(
                result.status(),
                result.endpointCode(),
                result.localPath(),
                result.remotePath(),
                result.checksum(),
                result.fileSize(),
                result.completedAt(),
                result.detail());
    }

    public CpfRemoteCommandPlan planRemoteCommand(
            String host,
            int port,
            String username,
            String command,
            String requestUser) {
        requiredText(host, "host");
        requiredText(username, "username");
        requiredText(command, "command");
        List<String> plan = List.of(
                "ssh",
                "-p",
                String.valueOf(port <= 0 ? 22 : port),
                username + "@" + host,
                command);
        CpfRemoteCommandPlan result = new CpfRemoteCommandPlan(
                true,
                false,
                0,
                plan,
                "",
                "원격 명령은 credential provider와 승인된 CPF adapter가 있어야 실행됩니다.");
        record("REMOTE_COMMAND", "SSH", "COMMAND", false, true, host,
                command, null, requestUser, result.detail());
        return result;
    }

    public List<CpfFileExchangeHistoryRecord> findRecentHistory() {
        return List.copyOf(history);
    }

    private Path resolveSafePath(String relativePath) {
        String value = requiredText(relativePath, "relativePath");
        Path requested = Path.of(value);
        if (requested.isAbsolute()) {
            throw new IllegalArgumentException("절대경로는 허용되지 않습니다.");
        }
        Path target = baseDir.resolve(requested).normalize();
        if (!target.startsWith(baseDir)) {
            throw new IllegalArgumentException("경로가 허용된 기준 디렉터리를 벗어났습니다: " + relativePath);
        }
        return target;
    }

    private void record(
            String action,
            String protocol,
            String direction,
            boolean executed,
            boolean success,
            String host,
            String source,
            String target,
            String requestUser,
            String detail) {
        history.addFirst(new CpfFileExchangeHistoryRecord(
                "FLE-" + UUID.randomUUID(),
                action,
                protocol,
                direction,
                executed,
                success,
                host,
                source,
                target,
                defaultUser(requestUser),
                detail,
                Instant.now()));
        while (history.size() > MAX_HISTORY) {
            history.pollLast();
        }
    }

    private String defaultCredential(String value) {
        return hasText(value) ? value : "CPF_FILE_TRANSFER_CREDENTIAL";
    }

    private String defaultUser(String value) {
        return hasText(value) ? value : "SYSTEM";
    }

    private String requiredText(String value, String fieldName) {
        if (!hasText(value)) {
            throw new IllegalArgumentException(fieldName + "는 필수입니다.");
        }
        return value;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String safeMessage(Throwable ex) {
        return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
    }
}
