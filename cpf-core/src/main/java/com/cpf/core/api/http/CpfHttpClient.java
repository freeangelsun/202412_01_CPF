package com.cpf.core.api.http;

/**
 * Provider-neutral service-to-service HTTP contract.
 * Implementations own transport, retry, timeout, circuit, tracing and transaction-header propagation.
 */
public interface CpfHttpClient {
    <T> T get(String serviceId, String relativePath, Class<T> responseType);
    <T> T get(String standardExecutionId, String serviceId, String relativePath, Class<T> responseType);
    <T> T get(String serviceId, String relativePath, CpfTypeRef<T> responseType);
    <T> T post(String serviceId, String relativePath, Object requestBody, Class<T> responseType);
    <T> T post(String serviceId, String relativePath, Object requestBody, CpfTypeRef<T> responseType);
}
