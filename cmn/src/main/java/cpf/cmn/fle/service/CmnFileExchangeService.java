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
import cpf.pfw.common.exception.FpsExternalServiceException;
import cpf.pfw.common.exception.FpsValidationException;
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
 * ?뚯씪/?먭꺽 ?곌퀎 怨듯넻 ?쒕퉬?ㅼ엯?덈떎.
 *
 * <p>濡쒖뺄 ?뚯씪 ?낆텧?μ? 利됱떆 ?ъ슜?????덇퀬, SSH/SCP/SFTP/FTP 媛숈? ?먭꺽 ?곌퀎?? * ?댁쁺 蹂댁븞 ?뺤콉???곕씪 ?덉슜 ?몄뒪?몄? ?ㅽ뻾 ?щ?瑜?낆떆?곸쑝濡?耳쒖빞 ?⑸땲??</p>
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
     * 湲곗? ?붾젆?곕━ ?꾨옒???띿뒪???뚯씪???앹꽦?⑸땲??
     *
     * @param relativePath 湲곗? ?붾젆?곕━ 湲곗? ?곷? 寃쎈줈
     * @param contents     ?뚯씪 ?댁슜
     * @return ?앹꽦???뚯씪 寃쎈줈
     */
    public Path writeText(String relativePath, String contents) {
        Path target = resolveSafePath(relativePath);
        try {
            Files.createDirectories(target.getParent());
            Files.writeString(target, contents == null ? "" : contents, StandardCharsets.UTF_8);
            recordHistory("LOCAL_WRITE", CmnFileProtocol.LOCAL.name(), "WRITE", true, true,
                    null, relativePath, target.toString(), "SYSTEM", "濡쒖뺄 ?뚯씪 ?곌린瑜??꾨즺?덉뒿?덈떎.");
            return target;
        } catch (IOException ex) {
            recordHistory("LOCAL_WRITE", CmnFileProtocol.LOCAL.name(), "WRITE", true, false,
                    null, relativePath, target.toString(), "SYSTEM", ex.getMessage());
            throw new FpsExternalServiceException("濡쒖뺄 ?뚯씪 ?곌린???ㅽ뙣?덉뒿?덈떎. path=" + target, ex);
        }
    }

    /**
     * 湲곗? ?붾젆?곕━ ?꾨옒???띿뒪???뚯씪???쎌뒿?덈떎.
     *
     * @param relativePath 湲곗? ?붾젆?곕━ 湲곗? ?곷? 寃쎈줈
     * @return ?뚯씪 ?댁슜
     */
    public String readText(String relativePath) {
        Path target = resolveSafePath(relativePath);
        try {
            String contents = Files.readString(target, StandardCharsets.UTF_8);
            recordHistory("LOCAL_READ", CmnFileProtocol.LOCAL.name(), "READ", true, true,
                    null, relativePath, target.toString(), "SYSTEM", "濡쒖뺄 ?뚯씪 ?쎄린瑜??꾨즺?덉뒿?덈떎.");
            return contents;
        } catch (IOException ex) {
            recordHistory("LOCAL_READ", CmnFileProtocol.LOCAL.name(), "READ", true, false,
                    null, relativePath, target.toString(), "SYSTEM", ex.getMessage());
            throw new FpsExternalServiceException("濡쒖뺄 ?뚯씪 ?쎄린???ㅽ뙣?덉뒿?덈떎. path=" + target, ex);
        }
    }

    /**
     * 湲곗? ?붾젆?곕━ ?꾨옒 ?뚯씪 ⑸줉??議고쉶?⑸땲??
     *
     * @param relativeDir 湲곗? ?붾젆?곕━ 湲곗? ?곷? ?붾젆?곕━
     * @return ?뚯씪 ⑸줉
     */
    public List<String> list(String relativeDir) {
        Path target = resolveSafePath(TextUtils.defaultIfBlank(relativeDir, "."));
        try (var stream = Files.list(target)) {
            List<String> files = stream.map(path -> baseDir().relativize(path).toString().replace("\\", "/")).toList();
            recordHistory("LOCAL_LIST", CmnFileProtocol.LOCAL.name(), "LIST", true, true,
                    null, relativeDir, target.toString(), "SYSTEM", "濡쒖뺄 ?뚯씪 ⑸줉 議고쉶瑜??꾨즺?덉뒿?덈떎.");
            return files;
        } catch (IOException ex) {
            recordHistory("LOCAL_LIST", CmnFileProtocol.LOCAL.name(), "LIST", true, false,
                    null, relativeDir, target.toString(), "SYSTEM", ex.getMessage());
            throw new FpsExternalServiceException("濡쒖뺄 ?뚯씪 ⑸줉 議고쉶???ㅽ뙣?덉뒿?덈떎. path=" + target, ex);
        }
    }

    /**
     * 濡쒖뺄/?먭꺽 ?뚯씪 ?꾩넚??泥섎━?⑸땲??
     *
     * <p>LOCAL? ?ㅼ젣 蹂듭궗?⑸땲?? SCP/SFTP/FTP??낅졊 怨꾪쉷??留뚮뱾怨?
     * {@code dryRun=false}, {@code sshEnabled=true}, ?덉슜 ?몄뒪??議곌굔??留뚯”???뚮쭔 ?ㅽ뻾?⑸땲??</p>
     *
     * @param request ?뚯씪 ?꾩넚 ?붿껌
     * @return ?뚯씪 ?꾩넚 寃곌낵
     */
    public CmnFileTransferResult transfer(CmnFileTransferRequest request) {
        if (request == null) {
            throw new FpsValidationException("?뚯씪 ?꾩넚 ?붿껌? ?꾩닔?낅땲??");
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
                    "?먭꺽 ?뚯씪 ?꾩넚 ?ㅽ뻾? 鍮꾪솢?깊솕?섏뼱 낅졊 怨꾪쉷留?諛섑솚?덉뒿?덈떎.",
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
     * SSH 낅졊???ㅽ뻾?섍굅???ㅽ뻾 怨꾪쉷??諛섑솚?⑸땲??
     *
     * @param request SSH 낅졊 ?붿껌
     * @return ?ㅽ뻾 寃곌낵
     */
    public CmnRemoteCommandResult runSshCommand(CmnRemoteCommandRequest request) {
        if (request == null || !TextUtils.hasText(request.command())) {
            throw new FpsValidationException("SSH ?ㅽ뻾 낅졊? ?꾩닔?낅땲??");
        }
        List<String> command = buildSshCommand(request);
        if (!properties.isSshEnabled()) {
            CmnRemoteCommandResult result = new CmnRemoteCommandResult(
                    true,
                    false,
                    0,
                    command,
                    "",
                    "SSH 낅졊 ?ㅽ뻾? 鍮꾪솢?깊솕?섏뼱 낅졊 怨꾪쉷留?諛섑솚?덉뒿?덈떎.");
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
                "SSH 낅졊 ?ㅽ뻾???꾨즺?덉뒿?덈떎.");
        recordHistory("SSH_COMMAND", "SSH", "COMMAND", true, result.success(),
                request.host(), request.command(), null, request.requestUser(), result.message());
        return result;
    }

    /**
     * 理쒓렐 ?뚯씪/?먭꺽 ?곌퀎 ?대젰??議고쉶?⑸땲??
     *
     * @return 理쒓렐 ?대젰 ⑸줉
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
                    "濡쒖뺄 ?뚯씪 蹂듭궗瑜??꾨즺?덉뒿?덈떎.",
                    source.toString(),
                    target.toString());
            recordHistory("LOCAL_COPY", CmnFileProtocol.LOCAL.name(), "COPY", true, true,
                    null, source.toString(), target.toString(), request.requestUser(), result.message());
            return result;
        } catch (IOException ex) {
            recordHistory("LOCAL_COPY", CmnFileProtocol.LOCAL.name(), "COPY", true, false,
                    null, source.toString(), target.toString(), request.requestUser(), ex.getMessage());
            throw new FpsExternalServiceException("濡쒖뺄 ?뚯씪 蹂듭궗???ㅽ뙣?덉뒿?덈떎. source="
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

        throw new FpsValidationException("吏?먰븯吏 ?딅뒗 ?뚯씪 ?꾩넚 ?꾨줈?좎퐳?낅땲?? protocol=" + protocol);
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
                return new ProcessResult(124, "?몃? 낅졊????꾩븘?껋쑝濡?醫낅즺?섏뿀?듬땲??");
            }
            String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            return new ProcessResult(process.exitValue(), output);
        } catch (IOException ex) {
            throw new FpsExternalServiceException("?몃? 낅졊 ?ㅽ뻾???ㅽ뙣?덉뒿?덈떎. command=" + command, ex);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new FpsExternalServiceException("?몃? 낅졊 ?ㅽ뻾??以묐떒?섏뿀?듬땲?? command=" + command, ex);
        }
    }

    private void validateRemoteRequest(String host, String username) {
        if (!TextUtils.hasText(host)) {
            throw new FpsValidationException("?먭꺽 ?몄뒪?몃뒗 ?꾩닔?낅땲??");
        }
        if (!TextUtils.hasText(username)) {
            throw new FpsValidationException("?먭꺽 ?ъ슜?먮뒗 ?꾩닔?낅땲??");
        }
    }

    private void validateAllowedHost(String host) {
        if (!properties.getAllowedHosts().contains(host)) {
            throw new FpsValidationException("?덉슜?섏? ?딆? ?먭꺽 ?몄뒪?몄엯?덈떎. host=" + host);
        }
    }

    private Path resolveSafePath(String relativePath) {
        if (!TextUtils.hasText(relativePath)) {
            throw new FpsValidationException("?뚯씪 寃쎈줈???꾩닔?낅땲??");
        }
        Path baseDir = baseDir();
        Path target = baseDir.resolve(relativePath).normalize();
        if (!target.startsWith(baseDir)) {
            throw new FpsValidationException("湲곗? ?붾젆?곕━ 諛뽰쓽 ?뚯씪? ?묎렐?????놁뒿?덈떎. path=" + relativePath);
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

