package com.cpf.integration.http.internal.domaincall;

import com.cpf.core.api.result.CpfRecoveryInfo;
import com.cpf.core.api.result.CpfResult;
import com.cpf.core.api.result.CpfResultStatus;

/** HTTP adapter 내부에서 표준 Result를 JSON으로 전달하는 envelope입니다. */
public record CpfDomainRemoteEnvelope(
        CpfResultStatus status, Object data, String errorCode, String errorMessage,
        String recoveryId, String recoveryAction) {
    static CpfDomainRemoteEnvelope from(CpfResult<?> result) {
        CpfRecoveryInfo recovery = result.recoveryInfo();
        return new CpfDomainRemoteEnvelope(result.status(), result.data(), result.errorCode(), result.errorMessage(),
                recovery == null ? null : recovery.recoveryId(), recovery == null ? null : recovery.action());
    }
}
