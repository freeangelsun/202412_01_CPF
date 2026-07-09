package cpf.exs.edu.fixedlength;

import cpf.cmn.edu.fixedlength.CmnFixedLengthFormatterEducationSample;

/**
 * 대외 송신 전문 adapter가 CMN formatter를 사용하는 샘플입니다.
 */
public class ExsFixedLengthSendAdapterEducationSample {

    public String buildSendTelegram(String channel, String memberNo, String status) {
        return new CmnFixedLengthFormatterEducationSample()
                .formatMember(channel, memberNo, status)
                .message();
    }
}
