package com.cpf.integration.http.api;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.util.UriBuilder;
import java.net.URI;
import java.util.function.Function;

/** 서비스 간 HTTP 호출을 표준 ServiceCallEngine에 위임하는 공개 API다. 구현은 header 전파, timeout/retry/circuit를 적용하며 구현체는 thread-safe 해야 한다. */
public interface CpfHttpClient {
    /** GET 호출을 수행한다. @param serviceId 등록 서비스 ID(필수) @param uriFunction URI 생성 함수(필수) @param responseType 응답 타입(필수) @return 역직렬화 결과, 계약상 null을 반환하지 않음 @throws RuntimeException 호출/역직렬화 실패 시. 호출은 원격 side effect가 없다고 가정하나 retry 정책이 적용될 수 있다. */
    <T> T get(String serviceId, Function<UriBuilder, URI> uriFunction, Class<T> responseType);
    /** 표준 실행 ID를 지정한 GET을 수행한다. @param standardExecutionId 실행 상관 ID(필수) @param serviceId 서비스 ID @param uriFunction URI 생성 함수 @param responseType 응답 타입 @return 응답 객체(비-null) @throws RuntimeException 호출 실패 시. transactionId와 별개인 실행 ID를 로그/추적에 사용한다. */
    <T> T get(String standardExecutionId, String serviceId, Function<UriBuilder, URI> uriFunction, Class<T> responseType);
    /** 제네릭 응답 GET을 수행한다. @param serviceId 서비스 ID @param uriFunction URI 생성 함수 @param responseType 제네릭 타입 토큰 @return 응답 객체(비-null) @throws RuntimeException 호출 실패 시. */
    <T> T get(String serviceId, Function<UriBuilder, URI> uriFunction, ParameterizedTypeReference<T> responseType);
    /** POST 호출을 수행한다. @param serviceId 서비스 ID @param path 상대 경로 @param requestBody 요청 본문(null 허용 여부는 대상 API 계약에 따름) @param responseType 응답 타입 @return 응답 객체 @throws RuntimeException 호출 실패 시. POST retry는 하위 idempotency/retry 정책을 따라야 한다. */
    <T> T post(String serviceId, String path, Object requestBody, Class<T> responseType);
    /** 제네릭 응답 POST를 수행한다. @param serviceId 서비스 ID @param path 상대 경로 @param requestBody 요청 본문 @param responseType 제네릭 타입 토큰 @return 응답 객체 @throws RuntimeException 호출 실패 시. 원격 side effect 중복 방지는 호출자가 idempotency 계약을 제공해야 한다. */
    <T> T post(String serviceId, String path, Object requestBody, ParameterizedTypeReference<T> responseType);
}
