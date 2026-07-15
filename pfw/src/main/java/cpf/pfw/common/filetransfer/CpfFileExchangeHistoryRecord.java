package cpf.pfw.common.filetransfer;

import java.time.Instant;

/**
 * 파일 helper, 전송, 원격 명령 계획을 같은 형식으로 추적하는 PFW 이력입니다.
 */
public record CpfFileExchangeHistoryRecord(
        String exchangeId,
        String actionType,
        String protocol,
        String direction,
        boolean executed,
        boolean success,
        String host,
        String sourcePath,
        String targetPath,
        String requestUser,
        String detail,
        Instant createdAt) {
}
