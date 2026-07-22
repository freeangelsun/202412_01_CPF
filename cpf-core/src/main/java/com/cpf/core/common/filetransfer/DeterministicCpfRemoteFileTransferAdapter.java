package com.cpf.core.common.filetransfer;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * 실제 외부 서버 없이 원격 파일전송 port 계약을 검증하는 결정적 reference adapter입니다.
 *
 * <p>SFTP/FTP/FTPS/SCP/SSH의 네트워크 인증이나 wire protocol을 흉내 내지 않습니다. 대신 허용된
 * 로컬 경로와 프로토콜별 격리 디렉터리를 사용해 경로 검증, checksum, 임시 파일, 원자 이동,
 * 중복 파일 정책을 실제 CPF 전송 엔진과 함께 검증합니다.</p>
 */
public class DeterministicCpfRemoteFileTransferAdapter implements CpfFileTransferPort {
    private static final Set<CpfFileTransferProtocol> SUPPORTED = Set.of(
            CpfFileTransferProtocol.SFTP,
            CpfFileTransferProtocol.FTP,
            CpfFileTransferProtocol.FTPS,
            CpfFileTransferProtocol.SCP,
            CpfFileTransferProtocol.SSH);

    private final Path harnessRemoteRoot;
    private final Path allowedLocalBase;
    private final LocalCpfFileTransferAdapter localAdapter;

    public DeterministicCpfRemoteFileTransferAdapter(Path harnessRemoteRoot, Path allowedLocalBase) {
        this.harnessRemoteRoot = requiredRoot(harnessRemoteRoot, "harnessRemoteRoot");
        this.allowedLocalBase = requiredRoot(allowedLocalBase, "allowedLocalBase");
        this.localAdapter = new LocalCpfFileTransferAdapter();
    }

    @Override
    public CpfFileTransferResult execute(CpfFileTransferEndpoint endpoint, CpfFileTransferRequest request) {
        CpfFileTransferProtocol protocol = protocol(endpoint.protocol());
        if (!SUPPORTED.contains(protocol)) {
            throw new IllegalArgumentException("remote reference adapter가 지원하지 않는 protocol입니다: " + protocol);
        }
        if (endpoint.credentialRef() == null) {
            throw new IllegalArgumentException("원격 파일전송 credentialRef는 필수입니다.");
        }

        Map<String, String> attributes = new LinkedHashMap<>(endpoint.attributes());
        attributes.put("localBasePath", allowedLocalBase.toString());
        attributes.put("referenceProtocol", protocol.name());
        CpfFileTransferEndpoint localEndpoint = new CpfFileTransferEndpoint(
                endpoint.endpointCode(),
                CpfFileTransferProtocol.LOCAL.name(),
                "localhost",
                0,
                harnessRemoteRoot.resolve(endpoint.endpointCode()).resolve(protocol.name()).toString(),
                endpoint.credentialRef(),
                endpoint.timeout(),
                attributes);
        CpfFileTransferRequest localRequest = new CpfFileTransferRequest(
                request.transactionGlobalId(),
                request.segmentId(),
                request.endpointCode(),
                request.operation(),
                request.localPath(),
                relativeRemotePath(request.remotePath()),
                normalizeChecksum(request.checksum()),
                request.fileSize(),
                request.attributes());
        return localAdapter.execute(localEndpoint, localRequest);
    }

    private CpfFileTransferProtocol protocol(String value) {
        try {
            return CpfFileTransferProtocol.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("알 수 없는 파일전송 protocol입니다: " + value, ex);
        }
    }

    private String relativeRemotePath(String remotePath) {
        if (remotePath == null || remotePath.isBlank()) {
            throw new IllegalArgumentException("remotePath는 필수입니다.");
        }
        return remotePath.replace('\\', '/').replaceFirst("^/+", "");
    }

    private String normalizeChecksum(String checksum) {
        if (checksum == null || checksum.isBlank() || "sha256:pending".equalsIgnoreCase(checksum)) {
            return null;
        }
        return checksum.replaceFirst("(?i)^sha256:", "");
    }

    private static Path requiredRoot(Path path, String fieldName) {
        if (path == null) {
            throw new IllegalArgumentException(fieldName + "는 필수입니다.");
        }
        return path.toAbsolutePath().normalize();
    }
}
