package cpf.cmn.fle.service;

import cpf.cmn.fle.core.CmnFileExchangeHistoryRecord;
import cpf.cmn.fle.core.CmnFileProtocol;
import cpf.cmn.fle.core.CmnFileTransferDirection;
import cpf.cmn.fle.core.CmnFileTransferRequest;
import cpf.cmn.fle.core.CmnFileTransferResult;
import cpf.cmn.fle.core.CmnRemoteCommandRequest;
import cpf.cmn.fle.core.CmnRemoteCommandResult;
import cpf.pfw.common.filetransfer.CpfFileExchangeGateway;
import cpf.pfw.common.filetransfer.CpfFileExchangeHistoryRecord;
import cpf.pfw.common.filetransfer.CpfFileTransferResult;
import cpf.pfw.common.filetransfer.CpfRemoteCommandPlan;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.util.List;

/**
 * CMN 프로젝트 공통 파일 API를 PFW file-exchange gateway에 연결하는 호환 facade입니다.
 *
 * <p>CPF-OWNERSHIP:CMN_PROJECT_HELPER</p>
 * <p>CMN은 기존 프로젝트 DTO와 기본 요청 규칙만 유지합니다. 경로 검증, 파일 I/O, protocol 실행 계획,
 * credential 참조, 기술 이력 저장은 PFW가 담당합니다.</p>
 */
@Service
public class CmnFileExchangeService extends cpf.cmn.common.base.CmnBaseService {
    private final CpfFileExchangeGateway gateway;

    public CmnFileExchangeService(CpfFileExchangeGateway gateway) {
        this.gateway = gateway;
    }

    public Path writeText(String relativePath, String contents) {
        return gateway.writeText(relativePath, contents, "CMN");
    }

    public String readText(String relativePath) {
        return gateway.readText(relativePath, "CMN");
    }

    public List<String> list(String relativeDirectory) {
        return gateway.list(relativeDirectory, "CMN");
    }

    public CmnFileTransferResult transfer(CmnFileTransferRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("파일전송 요청은 필수입니다.");
        }
        CmnFileProtocol protocol = request.protocol() == null ? CmnFileProtocol.LOCAL : request.protocol();
        CmnFileTransferDirection direction = request.direction() == null
                ? CmnFileTransferDirection.UPLOAD
                : request.direction();
        CpfFileTransferResult result = gateway.transfer(
                protocol.name(),
                direction.name(),
                request.host(),
                request.port() == null ? 0 : request.port(),
                request.identityFile(),
                request.localPath(),
                request.remotePath(),
                request.requestUser());
        boolean executed = !"PLANNED".equals(result.status());
        boolean success = "SUCCESS".equals(result.status()) || "PLANNED".equals(result.status());
        return new CmnFileTransferResult(
                success,
                executed,
                protocol,
                List.of(protocol.name(), direction.name()),
                result.detail(),
                result.localPath(),
                result.remotePath());
    }

    public CmnRemoteCommandResult runSshCommand(CmnRemoteCommandRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("원격 명령 요청은 필수입니다.");
        }
        CpfRemoteCommandPlan plan = gateway.planRemoteCommand(
                request.host(),
                request.port() == null ? 22 : request.port(),
                request.username(),
                request.command(),
                request.requestUser());
        return new CmnRemoteCommandResult(
                plan.accepted(),
                plan.executed(),
                plan.exitCode(),
                plan.command(),
                plan.output(),
                plan.detail());
    }

    public List<CmnFileExchangeHistoryRecord> findRecentHistory() {
        return gateway.findRecentHistory().stream()
                .map(this::toCmnRecord)
                .toList();
    }

    private CmnFileExchangeHistoryRecord toCmnRecord(CpfFileExchangeHistoryRecord record) {
        return new CmnFileExchangeHistoryRecord(
                record.exchangeId(),
                record.actionType(),
                record.protocol(),
                record.direction(),
                record.executed(),
                record.success(),
                record.host(),
                record.sourcePath(),
                record.targetPath(),
                record.requestUser(),
                record.detail(),
                record.createdAt().toString());
    }
}
