package cpf.cmn.biz.sequence;

/**
 * CMN 공통 채번 발급 결과입니다.
 *
 * @param sequenceKey  채번 기준 키
 * @param businessArea 업무 영역
 * @param businessKey  업무 키
 * @param sequenceKind 채번 종류
 * @param channelCode  채널 코드
 * @param issuedNo     발급 번호
 * @param issuedValue  발급 일련번호
 * @param dateKey      일자 키
 */
public record CmnSequenceIssueResult(
        String sequenceKey,
        String businessArea,
        String businessKey,
        String sequenceKind,
        String channelCode,
        String issuedNo,
        long issuedValue,
        String dateKey) {

    /**
     * 기존 결과 생성 코드와의 호환을 위한 생성자입니다.
     */
    public CmnSequenceIssueResult(String sequenceKey, String issuedNo, long issuedValue, String dateKey) {
        this(sequenceKey, null, null, null, null, issuedNo, issuedValue, dateKey);
    }
}
