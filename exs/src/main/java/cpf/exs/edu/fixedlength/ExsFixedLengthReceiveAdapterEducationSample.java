package cpf.exs.edu.fixedlength;

import cpf.cmn.edu.fixedlength.CmnFixedLengthParserEducationSample;

import java.util.Map;

/**
 * 대외 수신 전문 adapter가 CMN parser를 사용하는 샘플입니다.
 */
public class ExsFixedLengthReceiveAdapterEducationSample {

    public Map<String, String> parseReceiveTelegram(String message) {
        return new CmnFixedLengthParserEducationSample().parseMember(message).fields();
    }
}
