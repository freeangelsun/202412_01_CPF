package cpf.pfw.common.logging;

import java.time.LocalDateTime;

/**
 * 런타임에 적용되는 동적 거래 로그 레벨 규칙입니다.
 *
 * @param ruleId 고유 규칙 ID
 * @param transactionId 특정 트랜잭션 글로벌 ID 조건
 * @param businessTransactionId 업무 거래 ID 조건
 * @param moduleId 모듈 조건. 비어 있으면 모듈을 제한하지 않습니다.
 * @param logLevel 적용할 로그 레벨
 * @param reason 운영자가 입력한 적용 사유
 * @param createdBy 등록자
 * @param createdAt 등록 일시
 * @param expiresAt 만료 일시
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
     * 기준 시각보다 만료 일시가 지났는지 확인합니다.
     */
    public boolean expired(LocalDateTime now) {
        return expiresAt != null && now.isAfter(expiresAt);
    }
}
