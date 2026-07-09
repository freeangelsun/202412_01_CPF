package cpf.cmn.edu.fixedlength;

import cpf.cmn.message.fixedlength.FixedLengthParseResult;

/**
 * formatter로 생성한 전문을 parser로 다시 검증하는 round-trip 샘플입니다.
 */
public class CmnFixedLengthRoundTripEducationSample {

    public FixedLengthParseResult roundTrip(String channel, String memberNo, String status) {
        String message = new CmnFixedLengthFormatterEducationSample()
                .formatMember(channel, memberNo, status)
                .message();
        return new CmnFixedLengthParserEducationSample().parseMember(message);
    }
}
