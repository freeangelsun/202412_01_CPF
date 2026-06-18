package cpf.pfw.common.logging;

import java.time.LocalDateTime;

/**
 * CPF 기능 설명입니다.
 *
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 * CPF 기능 설명입니다.
 */
public record DynamicLogLevelRule(
        String ruleId,
        String transactionId,
        String businessTransactionId,
        String moduleId,
        CpfLogLevel logLevel,
        String reason,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime expiresAt) {

    /**
     * CPF 기능 설명입니다.
     *
     * CPF 기능 설명입니다.
     * CPF 기능 설명입니다.
     */
    public boolean expired(LocalDateTime now) {
        return expiresAt != null && now.isAfter(expiresAt);
    }
}

