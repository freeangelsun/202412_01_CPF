package cpf.cmn.fle.service;

import cpf.cmn.fle.config.CmnFileExchangeProperties;
import cpf.cmn.fle.core.CmnFileProtocol;
import cpf.cmn.fle.core.CmnFileExchangeHistoryRecord;
import cpf.cmn.fle.core.CmnFileTransferDirection;
import cpf.cmn.fle.core.CmnFileTransferRequest;
import cpf.cmn.fle.core.CmnFileTransferResult;
import cpf.cmn.fle.core.CmnRemoteCommandRequest;
import cpf.cmn.fle.core.CmnRemoteCommandResult;
import cpf.cmn.utils.DateTimeUtils;
import cpf.cmn.utils.IdUtils;
import cpf.cmn.utils.TextUtils;
import cpf.pfw.common.exception.CpfExternalServiceException;
import cpf.pfw.common.exception.CpfValidationException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.TimeUnit;

/**
 * CMN 파일 교환 교육/프로젝트 helper 서비스입니다.
 *
 * <p>CPF-OWNERSHIP:PFW_PORT_MIGRATION_CANDIDATE</p>
 * <p>로컬 파일 helper는 CMN에 둘 수 있지만, SFTP/FTP/SSH 명령 실행은 PFW filetransfer/runtime
 * port adapter로 이동해야 하는 기술 engine 후보입니다. 이번 단계에서는 기존 호환성을 유지하고
 * architecture scan과 gap에 후속 이동 대상으로 남깁니다.</p>
 */
@Service
public class CmnFileExchangeService {
    private static final int MAX_HISTORY_SIZE = 200;

    private final CmnFileExchangeProperties properties;
    private final Deque<CmnFileExchangeHistoryRecord> history = new ConcurrentLinkedDeque<>();

    public CmnFileExchangeService(CmnFileExchangeProperties properties) {
        this.properties = properties;
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    public Path writeText(String relativePath, String contents) {
        Path target = resolveSafePath(relativePath);
        try {
            Files.createDirectories(target.getParent());
            Files.writeString(target, contents == null ? "" : contents, StandardCharsets.UTF_8);
            recordHistory("LOCAL_WRITE", CmnFileProtocol.LOCAL.name(), "WRITE", true, true,
                    null, relativePath, target.toString(), "SYSTEM", "CPF 처리 기준입니다.");
            return target;
        } catch (IOException ex) {
            recordHistory("LOCAL_WRITE", CmnFileProtocol.LOCAL.name(), "WRITE", true, false,
                    null, relativePath, target.toString(), "SYSTEM", ex.getMessage());
            throw new CpfExternalServiceException("CPF 처리 기준입니다." + target, ex);
        }
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    public String readText(String relativePath) {
        Path target = resolveSafePath(relativePath);
        try {
            String contents = Files.readString(target, StandardCharsets.UTF_8);
            recordHistory("LOCAL_READ", CmnFileProtocol.LOCAL.name(), "READ", true, true,
                    null, relativePath, target.toString(), "SYSTEM", "CPF 처리 기준입니다.");
            return contents;
        } catch (IOException ex) {
            recordHistory("LOCAL_READ", CmnFileProtocol.LOCAL.name(), "READ", true, false,
                    null, relativePath, target.toString(), "SYSTEM", ex.getMessage());
            throw new CpfExternalServiceException("CPF 처리 기준입니다." + target, ex);
        }
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    public List<String> list(String relativeDir) {
        Path target = resolveSafePath(TextUtils.defaultIfBlank(relativeDir, "."));
        try (var stream = Files.list(target)) {
            List<String> files = stream.map(path -> baseDir().relativize(path).toString().replace("\\", "/")).toList();
            recordHistory("LOCAL_LIST", CmnFileProtocol.LOCAL.name(), "LIST", true, true,
                    null, relativeDir, target.toString(), "SYSTEM", "CPF 처리 기준입니다.");
            return files;
        } catch (IOException ex) {
            recordHistory("LOCAL_LIST", CmnFileProtocol.LOCAL.name(), "LIST", true, false,
                    null, relativeDir, target.toString(), "SYSTEM", ex.getMessage());
            throw new CpfExternalServiceException("CPF 처리 기준입니다." + target, ex);
        }
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    public CmnFileTransferResult transfer(CmnFileTransferRequest request) {
        if (request == null) {
            throw new CpfValidationException("CPF 처리 기준입니다.");
        }
        CmnFileProtocol protocol = request.protocol() == null ? CmnFileProtocol.LOCAL : request.protocol();
        CmnFileTransferDirection direction = request.direction() == null
                ? CmnFileTransferDirection.UPLOAD
                : request.direction();
        if (protocol == CmnFileProtocol.LOCAL) {
            return copyLocal(request);
        }
        List<String> command = buildTransferCommand(request);
        if (properties.isDryRun() || !properties.isSshEnabled()) {
            CmnFileTransferResult result = new CmnFileTransferResult(
                    true,
                    false,
                    protocol,
                    command,
                    "CPF 처리 기준입니다.",
                    request.localPath(),
                    request.remotePath());
            recordHistory("REMOTE_TRANSFER", protocol.name(), direction.name(), false, true,
                    request.host(), request.localPath(), request.remotePath(), request.requestUser(), result.message());
            return result;
        }
        validateAllowedHost(request.host());
        ProcessResult processResult = runProcess(command, Duration.ofSeconds(properties.getTimeoutSeconds()));
        CmnFileTransferResult result = new CmnFileTransferResult(
                processResult.exitCode() == 0,
                true,
                protocol,
                command,
                processResult.output(),
                request.localPath(),
                request.remotePath());
        recordHistory("REMOTE_TRANSFER", protocol.name(), direction.name(), true, result.success(),
                request.host(), request.localPath(), request.remotePath(), request.requestUser(), result.message());
        return result;
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    public CmnRemoteCommandResult runSshCommand(CmnRemoteCommandRequest request) {
        if (request == null || !TextUtils.hasText(request.command())) {
            throw new CpfValidationException("CPF 처리 기준입니다.");
        }
        List<String> command = buildSshCommand(request);
        if (!properties.isSshEnabled()) {
            CmnRemoteCommandResult result = new CmnRemoteCommandResult(
                    true,
                    false,
                    0,
                    command,
                    "",
                    "CPF 처리 기준입니다.");
            recordHistory("SSH_COMMAND", "SSH", "COMMAND", false, true,
                    request.host(), request.command(), null, request.requestUser(), result.message());
            return result;
        }
        validateAllowedHost(request.host());
        ProcessResult processResult = runProcess(command, Duration.ofSeconds(properties.getTimeoutSeconds()));
        CmnRemoteCommandResult result = new CmnRemoteCommandResult(
                processResult.exitCode() == 0,
                true,
                processResult.exitCode(),
                command,
                processResult.output(),
                "CPF 처리 기준입니다.");
        recordHistory("SSH_COMMAND", "SSH", "COMMAND", true, result.success(),
                request.host(), request.command(), null, request.requestUser(), result.message());
        return result;
    }

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     */
    public List<CmnFileExchangeHistoryRecord> findRecentHistory() {
        return history.stream().toList();
    }

    private CmnFileTransferResult copyLocal(CmnFileTransferRequest request) {
        Path source = resolveSafePath(request.localPath());
        Path target = resolveSafePath(request.remotePath());
        try {
            Files.createDirectories(target.getParent());
            Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            CmnFileTransferResult result = new CmnFileTransferResult(
                    true,
                    true,
                    CmnFileProtocol.LOCAL,
                    List.of("copy", source.toString(), target.toString()),
                    "CPF 처리 기준입니다.",
                    source.toString(),
                    target.toString());
            recordHistory("LOCAL_COPY", CmnFileProtocol.LOCAL.name(), "COPY", true, true,
                    null, source.toString(), target.toString(), request.requestUser(), result.message());
            return result;
        } catch (IOException ex) {
            recordHistory("LOCAL_COPY", CmnFileProtocol.LOCAL.name(), "COPY", true, false,
                    null, source.toString(), target.toString(), request.requestUser(), ex.getMessage());
            throw new CpfExternalServiceException("CPF 처리 기준입니다."
                    + source + ", target=" + target, ex);
        }
    }

    private void recordHistory(
            String actionType,
            String protocol,
            String direction,
            boolean executed,
            boolean success,
            String host,
            String sourcePath,
            String targetPath,
            String requestUser,
            String message) {
        history.addFirst(new CmnFileExchangeHistoryRecord(
                IdUtils.temporaryId("FLE"),
                actionType,
                protocol,
                direction,
                executed,
                success,
                host,
                sourcePath,
                targetPath,
                TextUtils.defaultIfBlank(requestUser, "SYSTEM"),
                message,
                DateTimeUtils.nowDateTimeMillis()));
        while (history.size() > MAX_HISTORY_SIZE) {
            history.pollLast();
        }
    }

    private List<String> buildTransferCommand(CmnFileTransferRequest request) {
        CmnFileProtocol protocol = request.protocol() == null ? CmnFileProtocol.SCP : request.protocol();
        CmnFileTransferDirection direction = request.direction() == null
                ? CmnFileTransferDirection.UPLOAD
                : request.direction();
        validateRemoteRequest(request.host(), request.username());

        if (protocol == CmnFileProtocol.SCP || protocol == CmnFileProtocol.SFTP) {
            List<String> command = new ArrayList<>();
            command.add(protocol == CmnFileProtocol.SCP ? "scp" : "sftp");
            if (TextUtils.hasText(request.identityFile())) {
                command.add("-i");
                command.add(request.identityFile());
            }
            if (request.port() != null) {
                command.add(protocol == CmnFileProtocol.SCP ? "-P" : "-P");
                command.add(String.valueOf(request.port()));
            }
            if (protocol == CmnFileProtocol.SCP) {
                String remote = request.username() + "@" + request.host() + ":" + request.remotePath();
                command.add(direction == CmnFileTransferDirection.UPLOAD ? request.localPath() : remote);
                command.add(direction == CmnFileTransferDirection.UPLOAD ? remote : request.localPath());
            } else {
                command.add(request.username() + "@" + request.host());
                command.add(direction == CmnFileTransferDirection.UPLOAD
                        ? "put " + request.localPath() + " " + request.remotePath()
                        : "get " + request.remotePath() + " " + request.localPath());
            }
            return command;
        }

        if (protocol == CmnFileProtocol.FTP) {
            return List.of(
                    "curl",
                    direction == CmnFileTransferDirection.UPLOAD ? "-T" : "-o",
                    direction == CmnFileTransferDirection.UPLOAD ? request.localPath() : request.localPath(),
                    "ftp://" + request.host() + "/" + request.remotePath());
        }

        throw new CpfValidationException("CPF 처리 기준입니다." + protocol);
    }

    private List<String> buildSshCommand(CmnRemoteCommandRequest request) {
        validateRemoteRequest(request.host(), request.username());
        List<String> command = new ArrayList<>();
        command.add("ssh");
        command.add("-o");
        command.add("BatchMode=yes");
        command.add("-o");
        command.add("ConnectTimeout=" + Math.max(1, properties.getTimeoutSeconds()));
        if (request.port() != null) {
            command.add("-p");
            command.add(String.valueOf(request.port()));
        }
        if (TextUtils.hasText(request.identityFile())) {
            command.add("-i");
            command.add(request.identityFile());
        }
        command.add(request.username() + "@" + request.host());
        command.add(request.command());
        return command;
    }

    private ProcessResult runProcess(List<String> command, Duration timeout) {
        try {
            Process process = new ProcessBuilder(command)
                    .redirectErrorStream(true)
                    .start();
            boolean finished = process.waitFor(timeout.toSeconds(), TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return new ProcessResult(124, "CPF 처리 기준입니다.");
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return new ProcessResult(process.exitValue(), output);
        } catch (IOException ex) {
            throw new CpfExternalServiceException("CPF 처리 기준입니다." + command, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new CpfExternalServiceException("CPF 처리 기준입니다." + command, ex);
        }
    }

    private void validateRemoteRequest(String host, String username) {
        if (!TextUtils.hasText(host)) {
            throw new CpfValidationException("CPF 처리 기준입니다.");
        }
        if (!TextUtils.hasText(username)) {
            throw new CpfValidationException("CPF 처리 기준입니다.");
        }
    }

    private void validateAllowedHost(String host) {
        if (!properties.getAllowedHosts().contains(host)) {
            throw new CpfValidationException("CPF 처리 기준입니다." + host);
        }
    }

    private Path resolveSafePath(String relativePath) {
        if (!TextUtils.hasText(relativePath)) {
            throw new CpfValidationException("CPF 처리 기준입니다.");
        }
        Path baseDir = baseDir();
        Path target = baseDir.resolve(relativePath).normalize();
        if (!target.startsWith(baseDir)) {
            throw new CpfValidationException("CPF 처리 기준입니다." + relativePath);
        }
        return target;
    }

    private Path baseDir() {
        return Path.of(properties.getBaseDir().replace("${java.io.tmpdir}", System.getProperty("java.io.tmpdir")))
                .toAbsolutePath()
                .normalize();
    }

    private record ProcessResult(int exitCode, String output) {
    }
}
