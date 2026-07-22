package com.cpf.core.common.remotelog;

import java.net.URI;

/** 서비스 registry가 제공하는 로그 조회 대상 실행 인스턴스입니다. */
public record CpfRemoteLogNode(
        String nodeId,
        String environment,
        String module,
        String service,
        String instance,
        URI managementUri,
        boolean local,
        boolean online,
        String mtlsProfile,
        String tokenAudience) {
}
