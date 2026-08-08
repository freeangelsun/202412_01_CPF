package com.cpf.core.common.http;

import com.cpf.core.api.http.CpfHttpClient;
import java.net.URI;
import java.util.function.Function;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.UriBuilder;

/** Spring transport extension; intentionally not part of cpf-core public API. */
public interface CpfSpringHttpClient extends CpfHttpClient {
    <T> T get(String serviceId, Function<UriBuilder, URI> uriFunction, Class<T> responseType);
    <T> T get(String standardExecutionId, String serviceId, Function<UriBuilder, URI> uriFunction, Class<T> responseType);
    <T> T get(String serviceId, Function<UriBuilder, URI> uriFunction, ParameterizedTypeReference<T> responseType);
    <T> T post(String serviceId, String path, Object requestBody, ParameterizedTypeReference<T> responseType);
}
