package com.cpf.web.runtime;

import com.cpf.foundation.annotation.CpfOnlineTransaction;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/** @CpfOnlineTransaction의 정적 메타데이터 계약을 fail-fast 검증합니다. */
final class CpfOnlineTransactionMetadataValidator {
    private static final Pattern ID = Pattern.compile("[A-Z][A-Z0-9_.:-]{2,159}");
    private static final Pattern OWNER = Pattern.compile("[A-Z][A-Z0-9_-]{1,31}");
    private static final Set<String> VISIBILITY = Set.of("PUBLIC", "INTERNAL");

    private CpfOnlineTransactionMetadataValidator() { }

    static void validate(CpfOnlineTransaction tx, String source) {
        if (tx == null) return;
        if (!ID.matcher(tx.id()).matches()) {
            throw new IllegalStateException("CPF_ONLINE_TX_ID_INVALID:" + source + ":" + tx.id());
        }
        if (tx.name() == null || tx.name().isBlank() || unsafe(tx.name())) {
            throw new IllegalStateException("CPF_ONLINE_TX_NAME_INVALID:" + source);
        }
        if (!OWNER.matcher(tx.ownerDomain()).matches()) {
            throw new IllegalStateException("CPF_ONLINE_TX_OWNER_INVALID:" + source + ":" + tx.ownerDomain());
        }
        String visibility = tx.visibility() == null ? "" : tx.visibility().trim().toUpperCase(Locale.ROOT);
        if (!VISIBILITY.contains(visibility) || !visibility.equals(tx.visibility())) {
            throw new IllegalStateException("CPF_ONLINE_TX_VISIBILITY_INVALID:" + source + ":" + tx.visibility());
        }
        if (unsafe(tx.requiredPermission())) {
            throw new IllegalStateException("CPF_ONLINE_TX_PERMISSION_INVALID:" + source);
        }
        if (!tx.gatewayAllowed() && !tx.directAllowed()) {
            throw new IllegalStateException("CPF_ONLINE_TX_NO_ALLOWED_ENTRY_PATH:" + source);
        }
    }

    private static boolean unsafe(String value) {
        return value != null && value.chars().anyMatch(Character::isISOControl);
    }
}
