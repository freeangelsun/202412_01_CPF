package com.cpf.core.api.async;
/** 최종 상태 전환 후 가벼운 후처리용 Listener. 장시간/전달보장 작업은 Outbox/Messaging/Webhook을 사용합니다. */
public interface CpfAsyncCompletionListener {
    default boolean supports(String operationId) { return true; }
    void onCompleted(CpfAsyncOperationStatus status);
}
