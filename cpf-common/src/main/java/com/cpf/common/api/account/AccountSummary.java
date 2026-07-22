package com.cpf.common.api.account;

/**
 * ACC가 소유하고 다른 주제영역에 제공하는 계정 요약 계약입니다.
 *
 * @param accountId 계정 내부 식별자
 * @param accountNo 계정 번호
 * @param accountName 계정명
 * @param statusCode 계정 상태 코드
 * @param version 낙관적 잠금 버전
 */
public record AccountSummary(
        long accountId,
        String accountNo,
        String accountName,
        String statusCode,
        long version) {
}
