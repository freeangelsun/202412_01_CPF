package com.cpf.core.api.result;

/** 결과불명 호출을 대사·재조회·수동확인으로 연결하는 복구 정보입니다. */
public record CpfRecoveryInfo(String recoveryId, String action) {
    public CpfRecoveryInfo {
        if (recoveryId == null || recoveryId.isBlank()) {
            throw new IllegalArgumentException("recoveryId는 필수입니다.");
        }
    }
}
