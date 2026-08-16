package com.cpf.file.api.filetransfer;

import java.time.Instant;

/** 공개 파일 송수신 결과입니다. UNKNOWN을 포함해 결과불명 거래를 숨기지 않습니다. */
public record CpfFileResult(String status,String endpointCode,String localPath,String remotePath,String checksum,long fileSize,Instant completedAt,String detail){
    public CpfFileResult { status=status==null||status.isBlank()?"UNKNOWN":status; completedAt=completedAt==null?Instant.now():completedAt; }
}
