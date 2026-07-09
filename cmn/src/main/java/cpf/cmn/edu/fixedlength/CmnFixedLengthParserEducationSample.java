package cpf.cmn.edu.fixedlength;

import cpf.cmn.message.fixedlength.FixedLengthMessageParser;
import cpf.cmn.message.fixedlength.FixedLengthParseResult;

/**
 * CMN 고정길이 parser 사용 샘플입니다.
 */
public class CmnFixedLengthParserEducationSample {

    public FixedLengthParseResult parseMember(String message) {
        return new FixedLengthMessageParser().parse(message, new CmnFixedLengthLayoutEducationSample().memberLayout());
    }
}
