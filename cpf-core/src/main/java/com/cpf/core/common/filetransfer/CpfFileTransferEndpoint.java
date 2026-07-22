package com.cpf.core.common.filetransfer;

import com.cpf.core.common.security.CpfCredentialRef;

import java.time.Duration;
import java.util.Map;

/**
 * SFTP/FTP/FTPS/SSH 파일 전송 endpoint 표준 정보입니다.
 */
public record CpfFileTransferEndpoint(
        String endpointCode,
        String protocol,
        String host,
        int port,
        String remoteBasePath,
        CpfCredentialRef credentialRef,
        Duration timeout,
        Map<String, String> attributes) {

    public CpfFileTransferEndpoint {
        if (endpointCode == null || endpointCode.isBlank()) {
            throw new IllegalArgumentException("endpointCode는 필수입니다.");
        }
        if (protocol == null || protocol.isBlank()) {
            throw new IllegalArgumentException("protocol은 필수입니다.");
        }
        timeout = timeout == null ? Duration.ofSeconds(30) : timeout;
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }
}
