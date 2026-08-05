package com.cpf.common.validation;

import com.cpf.common.dto.HeaderDTO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HeaderValidatorTest {
    private final HeaderValidator validator = new HeaderValidator();

    @Test
    void acceptsCompleteHeader() {
        assertDoesNotThrow(() -> validator.validate(header("tx-1", "MOBILE", "API", 1L)));
    }

    @Test
    void rejectsNullAndWhitespaceOnlyRequiredFields() {
        assertThrows(IllegalArgumentException.class, () -> validator.validate(null));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(header("   ", "MOBILE", "API", 1L)));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(header("tx-1", "\t", "API", 1L)));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(header("tx-1", "MOBILE", "\n", 1L)));
        assertThrows(IllegalArgumentException.class, () -> validator.validate(header("tx-1", "MOBILE", "API", null)));
    }

    private HeaderDTO header(String transactionId, String initialChannelCode, String channelCode, Long timestamp) {
        HeaderDTO header = new HeaderDTO();
        header.setTransactionId(transactionId);
        header.setInitialChannelCode(initialChannelCode);
        header.setChannelCode(channelCode);
        header.setTimestamp(timestamp);
        return header;
    }
}
