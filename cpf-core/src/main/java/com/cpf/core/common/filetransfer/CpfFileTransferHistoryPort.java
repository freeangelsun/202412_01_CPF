package com.cpf.core.common.filetransfer;

import java.time.Instant;
import java.util.List;

/**
 * 파일 전송 이력 적재/조회 port입니다.
 */
public interface CpfFileTransferHistoryPort {

    void record(CpfFileTransferRequest request, CpfFileTransferResult result);

    List<CpfFileTransferResult> findHistory(String endpointCode, Instant from, Instant to, int limit);
}
