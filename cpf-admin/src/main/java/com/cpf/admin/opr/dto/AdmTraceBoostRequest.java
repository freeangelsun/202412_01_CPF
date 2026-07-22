package cpf.adm.opr.dto;

/**
 * ADM 거래 단위 Trace Boost 요청입니다.
 *
 * <p>전체 root logger를 올리지 않고 특정 거래 조건에만 임시 로그 레벨 override를 적용합니다.</p>
 */
public record AdmTraceBoostRequest(
        Long policyId,
        String transactionGlobalId,
        String businessTransactionId,
        String apiPath,
        String status,
        String failureCode,
        Long durationMsGreaterThan,
        String logLevel,
        Long ttlSeconds,
        String requestUser,
        String reason) {
}
