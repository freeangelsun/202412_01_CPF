package com.cpf.platform.operations.observability.api.logging.policy;

import com.cpf.security.api.CpfSensitiveData;
import java.util.Objects;
import java.util.regex.Pattern;

/** Creates a new active version from an older retained version. */
/** CpfLogPolicyVersionRollbackCommand 타입의 역할과 책임을 정의하며 CPF 계약 경계를 명확히 유지한다. */
public record CpfLogPolicyVersionRollbackCommand(
        String commandId,
        LogPolicyTargetType targetType,
        String targetId,
        long expectedVersion,
        long targetVersion,
        String actor,
        String reason,
        CpfLogPolicyVersionApproval approval) {
    private static final Pattern IDENTIFIER = Pattern.compile("[A-Za-z0-9_.:-]{8,128}");
    // 트랜잭션·재시도·복구 경계의 의미를 보존해 부분 실패에서도 일관성을 유지한다.
    public CpfLogPolicyVersionRollbackCommand {
        commandId = identifier(commandId, "commandId");
        targetType = Objects.requireNonNull(targetType, "targetType");
        targetId = LogPolicyDecision.normalizeTargetId(targetId);
        if (expectedVersion < 2L || targetVersion < 1L || targetVersion >= expectedVersion) {
            throw new IllegalArgumentException("targetVersion must be positive and older than expectedVersion");
        }
        actor = identifier(actor, "actor");
        reason = CpfSensitiveData.sanitizeAuditReason(reason);
    }
    /** commandHash 작업을 CPF 표준 계약에 따라 수행한다. */
    public String commandHash() {
        return CpfLogPolicyVersionUpdateCommand.sha256("ROLLBACK|" + commandId + '|'
                + targetType.code() + '|' + targetId + '|' + expectedVersion + '|'
                + targetVersion + '|' + actor + '|' + reason);
    }
    private static String identifier(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " is required");
        String normalized = value.trim();
        if (!IDENTIFIER.matcher(normalized).matches()
                || normalized.chars().anyMatch(Character::isISOControl)) {
            throw new IllegalArgumentException(field + " is invalid");
        }
        return normalized;
    }
}
