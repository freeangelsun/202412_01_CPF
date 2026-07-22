package cpf.pfw.common.logging.policy;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 로그 정책 저장소 조회 계약입니다.
 */
public interface LogPolicyRepository {

    Optional<LogPolicyRow> findActiveOverride(LogPolicyTargetType targetType, String targetId, LocalDateTime now);

    Optional<LogPolicyRow> findActivePolicy(LogPolicyTargetType targetType, String targetId);
}
