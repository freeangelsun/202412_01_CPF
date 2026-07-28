package com.cpf.gateway.transport;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/** 설정된 Gateway 요청 본문 상한을 초과한 경우 발생합니다. */
@ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
public final class CpfGatewayPayloadTooLargeException extends RuntimeException {
    private final long maximumBytes;

    public CpfGatewayPayloadTooLargeException(long maximumBytes) {
        super("Gateway 요청 본문이 허용 크기를 초과했습니다. maximumBytes=" + maximumBytes);
        this.maximumBytes = maximumBytes;
    }

    public long maximumBytes() {
        return maximumBytes;
    }
}
