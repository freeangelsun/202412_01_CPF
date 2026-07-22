package com.cpf.core.common.admin;

/**
 * ADM credential 관제 후보 조회 조건입니다.
 */
public record CpfCredentialStatusQuery(
        String scope,
        String credentialId,
        String status) {
}
