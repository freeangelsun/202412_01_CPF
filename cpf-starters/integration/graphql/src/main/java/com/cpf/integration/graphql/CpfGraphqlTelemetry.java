package com.cpf.integration.graphql;

/** GraphQL latency/result 관측값을 Micrometer/OTel adapter가 소비하는 provider-neutral hook. */
@FunctionalInterface
public interface CpfGraphqlTelemetry {
    void record(String operationName, String result, long elapsedMillis);
    static CpfGraphqlTelemetry noop() { return (operationName, result, elapsedMillis) -> { }; }
}
