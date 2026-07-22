package cpf.pfw.common.filetransfer;

import java.time.Instant;

/**
 * 파일 전송 이력 조회 조건입니다.
 */
public record CpfFileTransferHistoryQuery(
        String endpointCode,
        String protocol,
        String status,
        String transactionGlobalId,
        Instant from,
        Instant to,
        int limit) {

    public CpfFileTransferHistoryQuery {
        limit = limit <= 0 ? 100 : Math.min(limit, 1000);
    }
}
