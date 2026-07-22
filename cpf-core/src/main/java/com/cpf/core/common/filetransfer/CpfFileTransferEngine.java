package com.cpf.core.common.filetransfer;

import com.cpf.core.common.reconciliation.CpfReconciliationPort;
import com.cpf.core.common.reconciliation.CpfUnknownResultRecord;

import java.time.Instant;
import java.util.Objects;

/**
 * 파일 전송 adapter 실행과 중복 방지, 이력, 결과 미확정 등록을 연결하는 CPF 표준 엔진입니다.
 */
public class CpfFileTransferEngine {
    private final CpfFileTransferPort transferPort;
    private final CpfFileTransferHistoryPort historyPort;
    private final CpfDuplicatePreventionPort duplicatePort;
    private final CpfReconciliationPort reconciliationPort;

    public CpfFileTransferEngine(
            CpfFileTransferPort transferPort,
            CpfFileTransferHistoryPort historyPort,
            CpfDuplicatePreventionPort duplicatePort,
            CpfReconciliationPort reconciliationPort) {
        this.transferPort = Objects.requireNonNull(transferPort, "transferPort는 필수입니다.");
        this.historyPort = Objects.requireNonNull(historyPort, "historyPort는 필수입니다.");
        this.duplicatePort = Objects.requireNonNull(duplicatePort, "duplicatePort는 필수입니다.");
        this.reconciliationPort = reconciliationPort;
    }

    public CpfFileTransferResult execute(CpfFileTransferEndpoint endpoint, CpfFileTransferRequest request) {
        String duplicateKey = duplicateKey(request);
        if (duplicatePort.alreadyProcessed(request.endpointCode(), duplicateKey, request.checksum())) {
            return new CpfFileTransferResult(
                    "DUPLICATE",
                    request.endpointCode(),
                    request.localPath(),
                    request.remotePath(),
                    request.checksum(),
                    request.fileSize(),
                    Instant.now(),
                    "같은 업무 key와 checksum의 성공 이력이 이미 존재합니다.");
        }

        try {
            CpfFileTransferResult result = transferPort.execute(endpoint, request);
            persistResult(request, result);
            return result;
        } catch (CpfFileTransferUnknownResultException ex) {
            CpfFileTransferResult unknown = new CpfFileTransferResult(
                    "UNKNOWN",
                    request.endpointCode(),
                    request.localPath(),
                    request.remotePath(),
                    request.checksum(),
                    request.fileSize(),
                    Instant.now(),
                    ex.getMessage());
            historyPort.record(request, unknown);
            registerUnknown(request, ex.getMessage());
            return unknown;
        }
    }

    private void persistResult(CpfFileTransferRequest request, CpfFileTransferResult result) {
        historyPort.record(request, result);
        if ("SUCCESS".equalsIgnoreCase(result.status()) && duplicatePort != historyPort) {
            duplicatePort.remember(request, result);
        }
    }

    private void registerUnknown(CpfFileTransferRequest request, String detail) {
        if (reconciliationPort == null) {
            return;
        }
        reconciliationPort.register(new CpfUnknownResultRecord(
                null,
                "FILE_TRANSFER",
                "CHECK_PENDING",
                request.transactionGlobalId(),
                request.segmentId(),
                duplicateKey(request),
                "CPF-FILE-UNKNOWN",
                detail,
                "CHECK_TARGET_AND_RETRY",
                Instant.now(),
                null));
    }

    private String duplicateKey(CpfFileTransferRequest request) {
        return request.attributes().getOrDefault(
                "businessKey",
                request.operation() + '|' + request.remotePath());
    }
}
