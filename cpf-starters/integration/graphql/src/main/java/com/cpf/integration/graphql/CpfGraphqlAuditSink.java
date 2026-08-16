package com.cpf.integration.graphql;

import java.time.Instant;

/** GraphQL 요청 결과를 민감 payload 없이 기록하는 감사 계약. */
@FunctionalInterface
public interface CpfGraphqlAuditSink {
    void record(Event event);

    record Event(String operationId, String operationName, String tenantId, String subjectId,
                 String transactionId, String documentSha256, String result, long elapsedMillis,
                 Instant occurredAt) { }

    static CpfGraphqlAuditSink noop() { return event -> { }; }
}
