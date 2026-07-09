package cpf.cmn.edu.fixedlength;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CmnFixedLengthParserEducationSampleTest {

    @Test
    void parserReadsFixedLengthFields() {
        assertThat(new CmnFixedLengthParserEducationSample().parseMember("WEB0000000001A").fields())
                .containsEntry("channel", "WEB")
                .containsEntry("memberNo", "0000000001");
    }
}
