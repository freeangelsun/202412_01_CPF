package com.cpf.core.common.header;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CpfInboundHeaderValidatorTest {
    private final CpfInboundHeaderValidator validator = new CpfInboundHeaderValidator(7);

    @Test
    void requiredHeadersAreReportedWhenMissing() {
        MockHttpServletRequest request = new MockHttpServletRequest();

        List<String> missing = validator.missingRequiredHeaders(request);

        assertThat(missing).containsExactly(
                CpfHeaderNames.TRANSACTION_ID,
                CpfHeaderNames.REQUEST_TYPE,
                CpfHeaderNames.ORIGINAL_CHANNEL_CODE,
                CpfHeaderNames.CHANNEL_CODE);
    }

    @Test
    void transactionIdMustFollowCpfGlobalIdFormat() {
        assertThat(validator.isValidTransactionId("20260615120000000MBRlocal010000001")).isTrue();
        assertThat(validator.isValidTransactionId("wrong-id")).isFalse();
    }
}
