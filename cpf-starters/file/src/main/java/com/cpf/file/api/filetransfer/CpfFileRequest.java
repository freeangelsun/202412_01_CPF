package com.cpf.file.api.filetransfer;

import java.util.Map;

/** 공개 파일 송수신 요청입니다. */
public record CpfFileRequest(String transactionId,String segmentId,String endpointCode,String operation,String localPath,String remotePath,String checksum,long fileSize,Map<String,String> attributes){
    public CpfFileRequest { if(endpointCode==null||endpointCode.isBlank()) throw new IllegalArgumentException("endpointCode는 필수입니다."); operation=operation==null||operation.isBlank()?"UPLOAD":operation; attributes=attributes==null?Map.of():Map.copyOf(attributes); }
}
