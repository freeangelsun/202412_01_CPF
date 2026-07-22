package cpf.cmn.biz.sequence;

/**
 * CMN 공통 채번 발급 요청입니다.
 *
 * @param sequenceKey    직접 지정할 채번 기준 키
 * @param businessArea   업무 영역
 * @param businessKey    업무 키
 * @param sequenceKind   채번 종류
 * @param channelCode    채널 코드
 * @param requestChannel 요청 채널
 * @param requestUser    요청 사용자
 * @param transactionId  프레임워크 거래 ID
 * @param traceId        분산 추적 ID
 */
public record CmnSequenceIssueRequest(
        String sequenceKey,
        String businessArea,
        String businessKey,
        String sequenceKind,
        String channelCode,
        String requestChannel,
        String requestUser,
        String transactionId,
        String traceId) {

    /**
     * 기존 sequenceKey 중심 호출 코드와의 호환을 위한 생성자입니다.
     */
    public CmnSequenceIssueRequest(
            String sequenceKey,
            String requestChannel,
            String requestUser,
            String transactionId,
            String traceId) {
        this(sequenceKey, null, null, null, null, requestChannel, requestUser, transactionId, traceId);
    }
}
