package com.cpf.core.common.transaction;

/**
 * CPF 온라인 거래 메타 정의입니다.
 *
 * <p>Controller의 {@code @CpfTransaction} 선언과 Spring MVC mapping 정보를 결합해
 * ADM에서 조회할 수 있는 거래 카탈로그로 저장합니다.</p>
 */
public record CpfTransactionMeta(
        String transactionId,
        String transactionName,
        String moduleCode,
        String domainCode,
        String httpMethod,
        String apiPath,
        String controllerClass,
        String handlerMethod,
        String swaggerOperationId,
        String logPolicyKey,
        String sensitiveYn,
        String maskingPolicyKey,
        String activeYn) {
}
