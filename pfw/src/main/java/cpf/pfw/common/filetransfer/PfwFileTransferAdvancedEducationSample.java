package cpf.pfw.common.filetransfer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;

/**
 * PFW 파일 전송 capability의 LOCAL adapter와 운영 이력 계약을 학습하기 위한 샘플입니다.
 *
 * <p>SFTP/FTP/SCP/SSH는 실제 외부 서버 검증이 필요하므로 protocol plan으로 남기고,
 * LOCAL adapter는 테스트에서 실제 파일 복사, 임시파일 rename, checksum, duplicate key를 검증합니다.</p>
 */
public class PfwFileTransferAdvancedEducationSample {

    /**
     * LOCAL adapter 기준 파일 복사 흐름입니다. 임시 파일에 먼저 쓰고 최종 파일로 rename 하여 불완전 파일 노출을 줄입니다.
     */
    public TransferScenario localCopy(Path sourceFile, Path targetDirectory, String transactionGlobalId) {
        try {
            Files.createDirectories(targetDirectory);
            String checksum = sha256(sourceFile);
            Path target = targetDirectory.resolve(sourceFile.getFileName().toString());
            Path temp = targetDirectory.resolve(sourceFile.getFileName().toString() + ".tmp");
            Files.copy(sourceFile, temp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            Files.move(temp, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            CpfFileTransferRequest request = new CpfFileTransferRequest(
                    transactionGlobalId,
                    "SEG-FILE-LOCAL-001",
                    "LOCAL-ARCHIVE",
                    "COPY",
                    sourceFile.toString(),
                    target.toString(),
                    checksum,
                    Files.size(sourceFile),
                    Map.of(
                            "protocol", CpfFileTransferProtocol.LOCAL.name(),
                            "tempSuffix", ".tmp",
                            "renamePolicy", "TEMP_THEN_RENAME"));
            CpfFileTransferResult result = CpfFileTransferResult.success(request, checksum, Files.size(target));
            TransferHistoryRecord history = new TransferHistoryRecord(
                    transactionGlobalId,
                    request.segmentId(),
                    request.endpointCode(),
                    CpfFileTransferProtocol.LOCAL,
                    result.status(),
                    duplicateKey(request),
                    result.checksum(),
                    result.fileSize(),
                    Instant.parse("2026-07-09T03:00:00Z"));
            return new TransferScenario(request, result, history, duplicateKey(request));
        } catch (IOException ex) {
            throw new IllegalStateException("LOCAL 파일 복사 샘플 실행에 실패했습니다.", ex);
        }
    }

    /**
     * 외부 서버가 필요한 프로토콜은 런타임 접속 대신 adapter가 지켜야 할 계획 항목으로 명확히 분리합니다.
     */
    public List<ProtocolPlan> protocolPlans() {
        return List.of(
                new ProtocolPlan(CpfFileTransferProtocol.SFTP, true, "credentialRef", "checksum+temp+rename"),
                new ProtocolPlan(CpfFileTransferProtocol.FTP, true, "credentialRef", "checksum+archive"),
                new ProtocolPlan(CpfFileTransferProtocol.SCP, true, "keyRef", "checksum+temp+rename"),
                new ProtocolPlan(CpfFileTransferProtocol.SSH, true, "keyRef", "remote-command-result"));
    }

    /**
     * 외부 전송 중 네트워크 단절처럼 성공/실패를 즉시 확정할 수 없는 상태를 명시적으로 표현합니다.
     */
    public CpfFileTransferResult unknownResult(CpfFileTransferRequest request) {
        return new CpfFileTransferResult(
                "UNKNOWN",
                request.endpointCode(),
                request.localPath(),
                request.remotePath(),
                request.checksum(),
                request.fileSize(),
                Instant.parse("2026-07-09T03:00:10Z"),
                "원격 서버 응답이 끊겨 후속 reconciliation이 필요합니다.");
    }

    public String duplicateKey(CpfFileTransferRequest request) {
        return request.endpointCode() + "|" + request.operation() + "|" + request.remotePath() + "|" + request.checksum();
    }

    private String sha256(Path path) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = Files.readAllBytes(path);
            return HexFormat.of().formatHex(digest.digest(bytes));
        } catch (IOException | NoSuchAlgorithmException ex) {
            throw new IllegalStateException("파일 checksum 계산에 실패했습니다.", ex);
        }
    }

    public record TransferScenario(
            CpfFileTransferRequest request,
            CpfFileTransferResult result,
            TransferHistoryRecord history,
            String duplicateKey) {
    }

    public record ProtocolPlan(
            CpfFileTransferProtocol protocol,
            boolean externalRuntimeRequired,
            String credentialPolicy,
            String transferPolicy) {
    }

    public record TransferHistoryRecord(
            String transactionGlobalId,
            String segmentId,
            String endpointCode,
            CpfFileTransferProtocol protocol,
            String status,
            String duplicateKey,
            String checksum,
            long fileSize,
            Instant loggedAt) {
    }
}
