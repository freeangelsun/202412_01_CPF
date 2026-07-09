package cpf.cmn.edu.fixedlength;

import cpf.cmn.message.fixedlength.FixedLengthFormatResult;
import cpf.cmn.message.fixedlength.FixedLengthMessageFormatter;

import java.util.Map;

/**
 * CMN 고정길이 formatter 사용 샘플입니다.
 */
public class CmnFixedLengthFormatterEducationSample {

    public FixedLengthFormatResult formatMember(String channel, String memberNo, String status) {
        return new FixedLengthMessageFormatter().format(
                Map.of("channel", channel, "memberNo", memberNo, "status", status),
                new CmnFixedLengthLayoutEducationSample().memberLayout());
    }
}
