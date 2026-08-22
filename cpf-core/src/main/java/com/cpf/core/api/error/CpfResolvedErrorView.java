package com.cpf.core.api.error;

import java.util.Locale;

/**
 * Error Catalog 해석 결과의 topology-independent 최소 조회 계약입니다.
 * DB/Common Product Service 구현 여부와 무관하게 Web/Batch/Broker 경계가 동일한 오류 의미를 소비합니다.
 */
public interface CpfResolvedErrorView {
    String responseCode();
    String messageCode();
    CpfErrorDefinition definition();
    String externalMessage();
    String internalMessage();
    Locale locale();
    boolean catalogHit();

    default CpfErrorDefinition.Category category() { return definition().category(); }
    default CpfErrorDefinition.RetryDisposition retryDisposition() { return definition().retryDisposition(); }
    default CpfErrorDefinition.Exposure exposure() { return definition().exposure(); }
}
