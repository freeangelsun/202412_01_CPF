package com.cpf.core.api.http;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.function.Function;

/**
 * 서비스 간 HTTP 호출의 Generated Domain 공개 계약입니다.
 * 구현은 ServiceCallEngine/Registry/retry/circuit/header propagation을 내부에서 적용합니다.
 */
public interface CpfHttpClient {
    <T> T get(String serviceId, Function<UriBuilder, URI> uriFunction, Class<T> responseType);
    <T> T get(String standardExecutionId, String serviceId, Function<UriBuilder, URI> uriFunction, Class<T> responseType);
    <T> T get(String serviceId, Function<UriBuilder, URI> uriFunction, ParameterizedTypeReference<T> responseType);
    <T> T post(String serviceId, String path, Object requestBody, Class<T> responseType);
    <T> T post(String serviceId, String path, Object requestBody, ParameterizedTypeReference<T> responseType);
}
