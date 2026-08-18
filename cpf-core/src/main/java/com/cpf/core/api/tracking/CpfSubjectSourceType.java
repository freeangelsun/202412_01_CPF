package com.cpf.core.api.tracking;

/** Subject 식별값을 확보한 신뢰 경계/Source를 구분합니다. */
public enum CpfSubjectSourceType {
    AUTHENTICATED_PRINCIPAL,
    TRUSTED_SESSION,
    TRUSTED_GATEWAY,
    SIGNED_CLIENT_METADATA,
    OPTIONAL_SUBJECT_HEADER,
    GENERATED_CONTRACT,
    LATE_IDENTITY_ENRICHMENT
}
