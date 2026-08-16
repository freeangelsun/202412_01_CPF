package com.cpf.education.operations.logging;
import java.util.Map;

/**
 * ADM 로그 관제와 연결할 운영 추적 키 샘플입니다.
 */
public class EducationOperationTraceEducationSample {

    public Map<String, String> traceKeys(String transactionId, String uri) {
        return Map.of(
                "transactionId", transactionId,
                "traceId", "TRACE-" + transactionId,
                "uri", uri,
                "admLink", "/adm/opr/logs?transactionId=" + transactionId);
    }
}
