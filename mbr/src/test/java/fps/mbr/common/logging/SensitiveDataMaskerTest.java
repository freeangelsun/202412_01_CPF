package fps.mbr.common.logging;

import fps.pfw.common.logging.SensitiveDataMasker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * PFW 민감정보 마스킹 유틸리티의 동작을 확인하는 테스트입니다.
 *
 * <p>거래 로그에는 파라미터, 요청 Body, 응답 Body가 남을 수 있으므로
 * 비밀번호, 인증 토큰, 계좌번호 같은 민감 값이 원문으로 저장되지 않는지 확인합니다.</p>
 */
class SensitiveDataMaskerTest {

    /**
     * JSON 문자열 안의 민감 키 값이 공백을 포함해도 마스킹되는지 확인합니다.
     */
    @Test
    void masksJsonStringValuesWithSpaces() {
        // password 값은 공백이 포함된 문자열이어도 로그 저장 전에 ***로 치환되어야 합니다.
        String masked = SensitiveDataMasker.mask("{\"password\":\"abc def\", \"name\":\"tester\"}");

        // 민감 키인 password는 마스킹되어야 합니다.
        assertThat(masked).contains("\"password\":\"***\"");
        // 원문 비밀번호는 어디에도 남지 않아야 합니다.
        assertThat(masked).doesNotContain("abc def");
        // 민감 키가 아닌 일반 값은 그대로 남아 로그 분석에 활용할 수 있어야 합니다.
        assertThat(masked).contains("\"name\":\"tester\"");
    }

    /**
     * 쿼리 파라미터와 Bearer 토큰 형식의 인증 정보가 마스킹되는지 확인합니다.
     */
    @Test
    void masksQueryParametersAndBearerTokens() {
        // accountNo와 Authorization은 금융 로그에서 반드시 원문 저장을 피해야 하는 대표 값입니다.
        String masked = SensitiveDataMasker.mask("accountNo=1234567890&Authorization=Bearer abc.def.ghi");

        // 계좌번호 파라미터는 값 전체가 ***로 치환되어야 합니다.
        assertThat(masked).contains("accountNo=***");
        // 인증 헤더 값도 토큰 원문 대신 ***로 치환되어야 합니다.
        assertThat(masked).contains("Authorization=***");
        // 실제 계좌번호가 로그 문자열에 남지 않았는지 확인합니다.
        assertThat(masked).doesNotContain("1234567890");
        // 실제 Bearer 토큰도 로그 문자열에 남지 않았는지 확인합니다.
        assertThat(masked).doesNotContain("abc.def.ghi");
    }
}
