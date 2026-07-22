package com.cpf.reference.logging;

import java.util.Map;

/**
 * ADM 로그 관제와 연결할 운영 추적 키 샘플입니다.
 */
public class ReferenceOperationTraceEducationSample {

    public Map<String, String> traceKeys(String transactionGlobalId, String uri) {
        return Map.of(
                "transactionGlobalId", transactionGlobalId,
                "traceId", "TRACE-" + transactionGlobalId,
                "uri", uri,
                "admLink", "/adm/opr/logs?transactionGlobalId=" + transactionGlobalId);
    }
}
