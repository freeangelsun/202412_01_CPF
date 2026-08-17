package com.cpf.web.runtime;

import com.cpf.foundation.execution.api.CpfOnlineTransaction;
import java.util.regex.Pattern;

/** @CpfOnlineTransaction의 정적 Operation Metadata 계약을 fail-fast 검증합니다. */
final class CpfOnlineTransactionMetadataValidator {
    private static final Pattern ID = Pattern.compile("[A-Za-z][A-Za-z0-9_.:-]{2,159}");
    private CpfOnlineTransactionMetadataValidator() { }

    static void validate(CpfOnlineTransaction tx, String source) {
        if (tx == null) return;
        if (tx.operationId() == null || !ID.matcher(tx.operationId()).matches()) {
            throw new IllegalStateException("CPF_OPERATION_ID_INVALID:" + source + ":" + tx.operationId());
        }
        if (blankOrUnsafe(tx.name())) throw new IllegalStateException("CPF_OPERATION_NAME_INVALID:" + source);
        if (blankOrUnsafe(tx.description())) throw new IllegalStateException("CPF_OPERATION_DESCRIPTION_INVALID:" + source);
    }

    private static boolean blankOrUnsafe(String value) {
        return value == null || value.isBlank() || value.chars().anyMatch(Character::isISOControl);
    }
}
