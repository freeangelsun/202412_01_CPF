package cpf.cmn.biz.sequence;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CmnSequenceServiceTest {

    @Test
    void formatIssuedNoUsesPrefixDateAndZeroPadding() {
        String issuedNo = CmnSequenceService.formatIssuedNo("EDU", "20260617", 12, 6);

        assertThat(issuedNo).isEqualTo("EDU20260617000012");
    }

    @Test
    void formatIssuedNoHandlesMissingDateKey() {
        String issuedNo = CmnSequenceService.formatIssuedNo("CMN", "", 7, 4);

        assertThat(issuedNo).isEqualTo("CMN0007");
    }

    @Test
    void legacyResultConstructorKeepsExistingCallersCompatible() {
        CmnSequenceIssueResult result = new CmnSequenceIssueResult("CMN_EDU_ORDER", "EDU000001", 1, "");

        assertThat(result.sequenceKey()).isEqualTo("CMN_EDU_ORDER");
        assertThat(result.issuedNo()).isEqualTo("EDU000001");
        assertThat(result.businessArea()).isNull();
    }
}
