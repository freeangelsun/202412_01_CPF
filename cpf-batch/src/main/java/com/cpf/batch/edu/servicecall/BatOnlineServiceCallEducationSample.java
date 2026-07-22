package com.cpf.batch.edu.servicecall;

import java.util.Map;

/**
 * 배치에서 온라인 주제영역을 호출할 때 헤더를 전달하는 샘플입니다.
 */
public class BatOnlineServiceCallEducationSample {

    public Map<String, String> buildHeaders(String transactionGlobalId, String jobExecutionId) {
        return Map.of(
                "x-cpf-transaction-global-id", transactionGlobalId,
                "x-cpf-batch-job-execution-id", jobExecutionId,
                "x-cpf-client-module", "BAT");
    }
}
