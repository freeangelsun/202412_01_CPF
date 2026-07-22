package cpf.cmn.biz.log;

/**
 * CMN 공통 업무 로그 등록 요청입니다.
 *
 * @param businessArea  업무 영역
 * @param businessKey   업무 키
 * @param logType       로그 유형
 * @param logMessage    로그 메시지
 * @param logPayload    상세 payload
 * @param requestUser   요청 사용자
 * @param transactionId 거래 ID
 * @param traceId       추적 ID
 */
public record CmnBusinessLogRequest(
        String businessArea,
        String businessKey,
        String logType,
        String logMessage,
        String logPayload,
        String requestUser,
        String transactionId,
        String traceId) {
}
