package com.cpf.member.common.logging;

import com.cpf.core.common.logging.SensitiveDataMasker;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** 로그에 기록되는 JSON·쿼리·인증 토큰의 민감정보 마스킹을 검증합니다. */
class SensitiveDataMaskerTest {

    /** 공백이 포함된 JSON 비밀번호도 전체 마스킹되는지 확인합니다. */
    @Test
    void masksJsonStringValuesWithSpaces() {
        String masked = SensitiveDataMasker.mask("{\"password\":\"abc def\", \"name\":\"tester\"}");

        assertThat(masked).contains("\"password\":\"***\"");
        assertThat(masked).doesNotContain("abc def");
        assertThat(masked).contains("\"name\":\"tester\"");
    }

    /** 계좌번호와 Bearer 토큰이 쿼리 문자열에서 노출되지 않는지 확인합니다. */
    @Test
    void masksQueryParametersAndBearerTokens() {
        String masked = SensitiveDataMasker.mask("accountNo=1234567890&Authorization=Bearer abc.def.ghi");

        assertThat(masked).contains("accountNo=***");
        assertThat(masked).contains("Authorization=***");
        assertThat(masked).doesNotContain("1234567890");
        assertThat(masked).doesNotContain("abc.def.ghi");
    }
}

