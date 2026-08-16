package com.cpf.file.api.filetransfer;

import java.time.Duration;
import java.util.Map;

/** Generated Domain이 사용하는 공개 파일전송 endpoint 계약입니다. */
public record CpfFileEndpoint(String endpointCode,String protocol,String host,int port,String remoteBasePath,CpfCredentialReference credential,Duration timeout,Map<String,String> attributes){
    public CpfFileEndpoint { if(endpointCode==null||endpointCode.isBlank()) throw new IllegalArgumentException("endpointCode는 필수입니다."); if(protocol==null||protocol.isBlank()) throw new IllegalArgumentException("protocol은 필수입니다."); timeout=timeout==null?Duration.ofSeconds(30):timeout; attributes=attributes==null?Map.of():Map.copyOf(attributes); }
}
