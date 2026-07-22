package cpf.pfw.common.admin;

import java.time.Instant;

/**
 * ADM 파일 전송 관제 후보 조회 조건입니다.
 */
public record CpfFileTransferStatusQuery(
        String endpointCode,
        String protocol,
        String status,
        Instant from,
        Instant to) {
}
