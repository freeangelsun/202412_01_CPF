package fps.pfw.common.logging;

import java.time.LocalDateTime;

/**
 * 운영 중 특정 거래의 진단 로그 레벨을 임시로 조정하기 위한 규칙입니다.
 *
 * @param ruleId                규칙 식별자
 * @param transactionId         글로벌 거래ID. 특정 한 건만 추적할 때 사용합니다.
 * @param businessTransactionId 컨트롤러 업무 거래ID. 특정 API만 추적할 때 사용합니다.
 * @param moduleId              주제영역 코드. 비어 있으면 전체 주제영역에 적용합니다.
 * @param logLevel              적용할 로그 레벨
 * @param reason                운영자가 규칙을 등록한 사유
 * @param createdBy             등록자
 * @param createdAt             등록 시각
 * @param expiresAt             만료 시각
 */
public record DynamicLogLevelRule(
        String ruleId,
        String transactionId,
        String businessTransactionId,
        String moduleId,
        FpsLogLevel logLevel,
        String reason,
        String createdBy,
        LocalDateTime createdAt,
        LocalDateTime expiresAt) {

    /**
     * 현재 시각 기준으로 규칙이 만료되었는지 확인합니다.
     *
     * @param now 현재 시각
     * @return 만료되었으면 true
     */
    public boolean expired(LocalDateTime now) {
        return expiresAt != null && now.isAfter(expiresAt);
    }
}
