package com.cpf.common.message.api;

/** Error Catalog fallback/invalid 상태를 감사·지표 계층에 전달하는 기술중립 신호 계약입니다. */
public interface CpfErrorCatalogSignalSink {
    void catalogFallback(String reason, String errorReference);
}
