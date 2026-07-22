package com.cpf.common.api.account;

/**
 * 주제영역 간 계정 요약 조회에 사용하는 중립 Facade Contract입니다.
 *
 * <p>동일 JVM에서는 ACC Local Adapter가, 분리 배포에서는 소비 모듈의 Remote Proxy가 구현합니다.
 * 소비자는 ACC Repository나 내부 Service를 직접 참조하지 않습니다.</p>
 */
public interface AccountSummaryFacade {
    /** 지정 계정의 마스킹된 업무 요약을 조회합니다. */
    AccountSummary findSummary(long accountId);
}
