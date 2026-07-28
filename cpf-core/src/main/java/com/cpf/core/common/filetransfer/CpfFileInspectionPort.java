package com.cpf.core.common.filetransfer;

/** Virus/DLP/Content scan과 quarantine을 실제 수행하는 확장 Port입니다. */
public interface CpfFileInspectionPort {
    Result inspect(CpfFileTransferRequest request, boolean quarantineOnFailure);
    record Result(boolean accepted, boolean quarantined, String reason) {}
}
